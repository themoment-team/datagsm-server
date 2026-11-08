package team.themoment.datagsm.oauth.authorization.domain.oauth.component

import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.student.repository.StudentDataEditRequestJpaRepository

/**
 * 비밀번호 입력 없이 로그인을 이어가는 경로(SSO 세션, 토큰 갱신)가 학생 정보 수정 강제를 우회하지 않도록,
 * 해소되지 않은 수정 요청이 있는지의 판정을 한곳에 모은다.
 */
@Component
class PendingStudentDataEditChecker(
    private val studentDataEditRequestJpaRepository: StudentDataEditRequestJpaRepository,
) {
    fun hasPendingRequest(account: AccountJpaEntity): Boolean {
        if (account.objectType != AccountObjectType.STUDENT) return false
        val studentId = account.objectId ?: return false
        return studentDataEditRequestJpaRepository.findByStudentId(studentId).isPresent
    }
}
