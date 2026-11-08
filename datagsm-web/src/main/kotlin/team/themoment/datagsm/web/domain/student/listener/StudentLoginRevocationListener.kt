package team.themoment.datagsm.web.domain.student.listener

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import team.themoment.datagsm.web.domain.student.dto.internal.StudentLoginRevocationRequested
import team.themoment.datagsm.web.domain.student.service.RevokeStudentLoginService

@Component
class StudentLoginRevocationListener(
    private val revokeStudentLoginService: RevokeStudentLoginService,
) {
    // 수정 요청이 커밋된 뒤에만 끊어야, 저장이 롤백됐을 때 학생이 이유 없이 로그아웃되지 않는다.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    fun handleRevocationRequested(event: StudentLoginRevocationRequested) {
        revokeStudentLoginService.revokeAsync(event.studentIds)
    }
}
