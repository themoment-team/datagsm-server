package team.themoment.datagsm.domain.student.entity.constant

import team.themoment.datagsm.domain.account.entity.constant.AccountRole

/**
 * 인증/인가와 관련 없이 학생의 역할을 정의하는 열거형 클래스입니다.
 * @see AccountRole
 */
enum class StudentRole {
    STUDENT_COUNCIL,
    DORMITORY_MANAGER,
    GENERAL_STUDENT,
}
