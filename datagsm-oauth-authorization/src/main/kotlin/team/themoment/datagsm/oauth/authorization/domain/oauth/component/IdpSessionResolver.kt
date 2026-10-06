package team.themoment.datagsm.oauth.authorization.domain.oauth.component

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.common.domain.student.repository.StudentDataEditRequestJpaRepository

/**
 * IdP 세션 쿠키만으로 비밀번호 재입력을 생략해도 되는 계정인지 판별한다.
 *
 * 세션으로 로그인을 건너뛰는 경로가 SSO 인가(GET /authorize)와 동의 승인(POST /authorize/consent)
 * 두 곳이라, 판정이 한쪽에만 적용돼 갈라지지 않도록 여기에 모은다.
 * 특히 학생 정보 수정 요청 확인은 강제 수정 절차가 SSO로 우회되는 것을 막는 불변 조건이다.
 */
@Component
class IdpSessionResolver(
    private val idpSessionRedisRepository: IdpSessionRedisRepository,
    private val accountJpaRepository: AccountJpaRepository,
    private val studentDataEditRequestJpaRepository: StudentDataEditRequestJpaRepository,
) {
    fun resolveEligibleAccount(sessionId: String?): AccountJpaEntity? {
        if (sessionId.isNullOrBlank()) return null

        val session = idpSessionRedisRepository.findByIdOrNull(sessionId) ?: return null
        val account = accountJpaRepository.findByEmail(session.email).orElse(null) ?: return null
        if (account.id == null) return null

        if (account.status != AccountStatus.ACTIVE) return null

        val studentId = account.objectId
        if (account.objectType == AccountObjectType.STUDENT && studentId != null) {
            val hasPendingEditRequest = studentDataEditRequestJpaRepository.findByStudentId(studentId).isPresent
            if (hasPendingEditRequest) return null
        }

        return account
    }
}
