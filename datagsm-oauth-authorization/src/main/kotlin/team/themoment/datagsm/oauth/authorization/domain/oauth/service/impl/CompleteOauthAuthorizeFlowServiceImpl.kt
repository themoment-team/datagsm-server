package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangeItem
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.dto.payload.StudentEventObject
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthAuthorizeSubmitReqDto
import team.themoment.datagsm.common.domain.oauth.entity.IdpSessionHandoffRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.IdpSessionRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.OauthConsentJpaEntity
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionHandoffRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthAuthorizeStateRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthConsentJpaRepository
import team.themoment.datagsm.common.domain.student.entity.DormitoryRoomNumber
import team.themoment.datagsm.common.domain.student.entity.StudentDataEditRequestJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.repository.StudentDataEditRequestJpaRepository
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.common.global.security.util.OpaqueTokenHashUtil
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.CompleteOauthAuthorizeFlowService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.IssueAuthorizationCodeService
import team.themoment.datagsm.oauth.authorization.global.security.service.OAuthClientRateLimitService
import team.themoment.sdk.exception.ExpectedException
import team.themoment.sdk.logging.logger.logger
import java.net.URI
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID

@Service
class CompleteOauthAuthorizeFlowServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
    private val clubJpaRepository: ClubJpaRepository,
    private val studentDataEditRequestJpaRepository: StudentDataEditRequestJpaRepository,
    private val oauthAuthorizeStateRedisRepository: OauthAuthorizeStateRedisRepository,
    private val idpSessionRedisRepository: IdpSessionRedisRepository,
    private val idpSessionHandoffRedisRepository: IdpSessionHandoffRedisRepository,
    private val oauthConsentJpaRepository: OauthConsentJpaRepository,
    private val issueAuthorizationCodeService: IssueAuthorizationCodeService,
    private val passwordEncoder: PasswordEncoder,
    private val oauthEnvironment: OauthEnvironment,
    private val oauthClientRateLimitService: OAuthClientRateLimitService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : CompleteOauthAuthorizeFlowService {
    companion object {
        private val secureRandom = SecureRandom()
    }

    @Transactional
    override fun execute(reqDto: OauthAuthorizeSubmitReqDto): ResponseEntity<Void> {
        val stateEntity =
            oauthAuthorizeStateRedisRepository
                .findById(reqDto.token)
                .orElseThrow {
                    OAuthException.InvalidRequest("인증 토큰이 유효하지 않거나 만료되었습니다. 다시 시도해주세요.")
                }

        val clientId = stateEntity.clientId
        val redirectUri = stateEntity.redirectUri
        val state = stateEntity.state
        val codeChallenge = stateEntity.codeChallenge
        val codeChallengeMethod = stateEntity.codeChallengeMethod
        val scopes = stateEntity.scopes

        val rateLimitResult = oauthClientRateLimitService.tryConsumeAndReturnRemaining(clientId)
        if (!rateLimitResult.consumed) {
            throw ExpectedException("요청 한도를 초과했습니다.", HttpStatus.TOO_MANY_REQUESTS)
        }

        if (!reqDto.email.endsWith("@gsm.hs.kr", ignoreCase = true)) {
            throw ExpectedException("이메일 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED)
        }

        val account =
            accountJpaRepository
                .findByEmail(reqDto.email)
                .orElseThrow { ExpectedException("이메일 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED) }

        if (!passwordEncoder.matches(reqDto.password, account.password)) {
            throw ExpectedException("이메일 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED)
        }

        if (account.status != AccountStatus.ACTIVE) {
            throw ExpectedException("아직 승인되지 않은 계정입니다.", HttpStatus.FORBIDDEN)
        }

        if (account.objectType == AccountObjectType.STUDENT && account.objectId != null) {
            resolveStudentDataEditRequestIfNeeded(account.objectId!!, reqDto)
        }

        val redirectUrl =
            issueAuthorizationCodeService.execute(
                email = account.email,
                clientId = clientId,
                redirectUri = redirectUri,
                state = state,
                codeChallenge = codeChallenge,
                codeChallengeMethod = codeChallengeMethod,
                scopes = scopes,
                nonce = stateEntity.nonce,
            )

        oauthAuthorizeStateRedisRepository.deleteById(reqDto.token)

        val accountId = requireNotNull(account.id) { "Persisted account must have an id" }
        recordConsent(accountId, clientId, scopes)

        val handoffUrl = createSessionHandoff(account.email, clientId, redirectUrl)

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(handoffUrl))
            .build()
    }

    // BFF가 서버-투-서버로 호출하므로 이 응답에는 브라우저 쿠키를 심을 수 없다.
    // 세션을 만들어두고, 브라우저가 최상위 이동으로 경유할 일회용 티켓 URL을 돌려준다.
    // ticket은 로그·Referer에 남을 수 있으므로 verifier를 함께 발급하고 해시만 저장한다.
    private fun createSessionHandoff(
        email: String,
        clientId: String,
        redirectUrl: String,
    ): String {
        val sessionId = UUID.randomUUID().toString()
        idpSessionRedisRepository.save(
            IdpSessionRedisEntity(
                sessionId = sessionId,
                email = email,
                // userAgent는 여기서 채우지 않는다. 이 요청은 BFF의 서버-투-서버 호출이라
                // User-Agent가 BFF의 것이다. 브라우저가 직접 오는 핸드오프 시점에 기록한다.
                createdAt = System.currentTimeMillis(),
                ttl = oauthEnvironment.idpSessionExpirationSeconds,
            ),
        )

        val ticket = generateOpaqueToken()
        val verifier = generateOpaqueToken()
        idpSessionHandoffRedisRepository.save(
            IdpSessionHandoffRedisEntity(
                ticket = ticket,
                verifierHash = OpaqueTokenHashUtil.hash(verifier),
                sessionId = sessionId,
                clientId = clientId,
                redirectUrl = redirectUrl,
                ttl = oauthEnvironment.idpSessionHandoffExpirationSeconds,
            ),
        )

        return UriComponentsBuilder
            .fromUriString(oauthEnvironment.issuerUrl)
            .path("/v1/oauth/authorize/session")
            .queryParam("ticket", ticket)
            .queryParam("verifier", verifier)
            .build()
            .toUriString()
    }

    // 같은 (account, client)로 첫 로그인이 동시에 들어오면 양쪽 다 insert를 시도해
    // uk_oauth_consent_account_client 위반이 난다. 이 시점에는 code와 세션이 이미
    // Redis에 저장돼 롤백되지 않으므로, 제약 위반은 재조회 후 병합으로 흡수한다.
    private fun recordConsent(
        accountId: Long,
        clientId: String,
        scopes: Set<String>,
    ) {
        try {
            saveConsent(accountId, clientId, scopes)
        } catch (e: DataIntegrityViolationException) {
            logger().warn("Retrying consent record after unique constraint violation for clientId {}", clientId, e)
            saveConsent(accountId, clientId, scopes)
        }
    }

    private fun saveConsent(
        accountId: Long,
        clientId: String,
        scopes: Set<String>,
    ) {
        val consent =
            oauthConsentJpaRepository
                .findByAccountIdAndClientId(accountId, clientId)
                .orElseGet { OauthConsentJpaEntity.create(accountId, clientId, emptySet()) }
        consent.grantedScopes.addAll(scopes)
        oauthConsentJpaRepository.saveAndFlush(consent)
    }

    private fun resolveStudentDataEditRequestIfNeeded(
        studentId: Long,
        reqDto: OauthAuthorizeSubmitReqDto,
    ) {
        val editRequest =
            studentDataEditRequestJpaRepository
                .findByStudentId(studentId)
                .orElse(null) ?: return

        if (!hasAllRequestedFields(editRequest, reqDto)) {
            throw ExpectedException("정보 수정이 필요합니다. 정보를 수정한 후 다시 로그인해주세요.", HttpStatus.UNPROCESSABLE_ENTITY)
        }

        val student =
            studentJpaRepository
                .findById(studentId)
                .orElseThrow { ExpectedException("학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }

        val old = generateStudentEventObject(student)
        applyRequestedFields(student, editRequest, reqDto)
        val new = generateStudentEventObject(student)

        studentDataEditRequestJpaRepository.delete(editRequest)

        applicationEventPublisher.publishEvent(
            EventDispatchRequested(
                EventType.STUDENT_UPDATED,
                EventChangedData(
                    old = listOf(EventChangeItem(0, old)),
                    new = listOf(EventChangeItem(0, new)),
                ),
            ),
        )
    }

    private fun hasAllRequestedFields(
        editRequest: StudentDataEditRequestJpaEntity,
        reqDto: OauthAuthorizeSubmitReqDto,
    ): Boolean {
        if (editRequest.requestStudentNumber &&
            (reqDto.studentGrade == null || reqDto.studentClass == null || reqDto.studentNumber == null)
        ) {
            return false
        }
        if (editRequest.requestDormitoryRoomNumber && reqDto.dormitoryRoomNumber == null) {
            return false
        }
        if (editRequest.requestMajorClub && reqDto.majorClubId == null) {
            return false
        }
        if (editRequest.requestAutonomousClub && reqDto.autonomousClubId == null) {
            return false
        }
        return true
    }

    private fun applyRequestedFields(
        student: StudentJpaEntity,
        editRequest: StudentDataEditRequestJpaEntity,
        reqDto: OauthAuthorizeSubmitReqDto,
    ) {
        val grade = reqDto.studentGrade
        val classNum = reqDto.studentClass
        val number = reqDto.studentNumber
        if (editRequest.requestStudentNumber && grade != null && classNum != null && number != null) {
            if (studentJpaRepository.existsByStudentNumberAndNotId(grade, classNum, number, student.id!!)) {
                throw ExpectedException("이미 존재하는 학번입니다.", HttpStatus.CONFLICT)
            }
            student.studentNumber = StudentNumber(grade, classNum, number)
        }
        if (editRequest.requestDormitoryRoomNumber) {
            student.dormitoryRoomNumber = DormitoryRoomNumber(reqDto.dormitoryRoomNumber)
        }
        val majorClubId = reqDto.majorClubId
        if (editRequest.requestMajorClub && majorClubId != null) {
            student.majorClub =
                clubJpaRepository
                    .findById(majorClubId)
                    .orElseThrow { ExpectedException("전공 동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }
        }
        val autonomousClubId = reqDto.autonomousClubId
        if (editRequest.requestAutonomousClub && autonomousClubId != null) {
            student.autonomousClub =
                clubJpaRepository
                    .findById(autonomousClubId)
                    .orElseThrow { ExpectedException("자율 동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }
        }
    }

    private fun generateStudentEventObject(student: StudentJpaEntity): StudentEventObject =
        StudentEventObject(
            studentId = student.id!!,
            name = student.name,
            email = student.email,
            sex = student.sex.name,
            grade = student.studentNumber?.studentGrade,
            classNum = student.studentNumber?.studentClass,
            number = student.studentNumber?.studentNumber,
            studentNumber = student.studentNumber?.fullStudentNumber,
            major = student.major?.name,
            specialty = student.specialty,
            role = student.role.name,
            dormitoryFloor = student.dormitoryRoomNumber?.dormitoryRoomFloor,
            dormitoryRoom = student.dormitoryRoomNumber?.dormitoryRoomNumber,
            majorClubName = student.majorClub?.name,
            autonomousClubName = student.autonomousClub?.name,
            githubId = student.githubId,
        )

    private fun generateOpaqueToken(): String =
        Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(ByteArray(22).also { secureRandom.nextBytes(it) })
}
