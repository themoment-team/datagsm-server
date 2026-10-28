package team.themoment.datagsm.web.domain.account.service.impl

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.dto.request.DeleteMyAccountReqDto
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthConsentJpaRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthRefreshTokenRedisRepository
import team.themoment.datagsm.common.domain.teacher.repository.TeacherJpaRepository
import team.themoment.datagsm.web.domain.account.service.DeleteMyAccountService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class DeleteMyAccountServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
    private val passwordEncoder: PasswordEncoder,
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val clientJpaRepository: ClientJpaRepository,
    private val accountJpaRepository: AccountJpaRepository,
    private val eventJpaRepository: EventJpaRepository,
    private val applicationJpaRepository: ApplicationJpaRepository,
    private val oauthConsentJpaRepository: OauthConsentJpaRepository,
    private val teacherJpaRepository: TeacherJpaRepository,
    private val idpSessionRedisRepository: IdpSessionRedisRepository,
    private val oauthRefreshTokenRedisRepository: OauthRefreshTokenRedisRepository,
) : DeleteMyAccountService {
    @Transactional
    override fun execute(reqDto: DeleteMyAccountReqDto) {
        val account = currentUserProvider.getCurrentAccount()

        if (!passwordEncoder.matches(reqDto.password, account.password)) {
            throw ExpectedException("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED)
        }

        // 계정을 FK로 참조하는 행이 남아 있으면 계정 삭제가 제약 위반으로 실패한다.
        eventJpaRepository.deleteAll(eventJpaRepository.findAllByAccount(account))
        deleteApplications(account)
        apiKeyJpaRepository.deleteByAccount(account)
        clientJpaRepository.deleteAllByAccount(account)
        account.id?.let { oauthConsentJpaRepository.deleteAllByAccountId(it) }
        deleteTeacher(account)
        accountJpaRepository.delete(account)

        // Redis는 트랜잭션에 묶이지 않지만, DB 삭제가 실패해도 로그아웃만 된 상태라 안전한 쪽으로 남는다.
        idpSessionRedisRepository.deleteAll(idpSessionRedisRepository.findAllByEmail(account.email))
        oauthRefreshTokenRedisRepository.deleteAll(oauthRefreshTokenRedisRepository.findAllByEmail(account.email))
    }

    // 애플리케이션 삭제 API와 같이, 다른 클라이언트에 부여된 이 애플리케이션의 scope를 먼저 걷어낸다.
    private fun deleteApplications(account: AccountJpaEntity) {
        applicationJpaRepository.findAllByAccount(account).forEach { application ->
            clientJpaRepository.removeScopesByApplicationId("${application.id}:")
            applicationJpaRepository.delete(application)
        }
    }

    // 선생님 정보는 가입 때 본인이 입력해 만든 행이라, 남기면 같은 이메일로 다시 가입할 수 없다.
    private fun deleteTeacher(account: AccountJpaEntity) {
        if (account.objectType != AccountObjectType.TEACHER) return
        account.objectId?.let { teacherJpaRepository.deleteById(it) }
    }
}
