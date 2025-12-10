package team.themoment.datagsm.domain.student.entity.constant

import team.themoment.datagsm.domain.account.entity.constant.AccountRole

/**
 * 인증/인가와 관련 없이 학생의 역할을 정의하는 열거형 클래스입니다.
 * @see AccountRole
 */
enum class StudentRole(val value: String) {
    STUDENT_COUNCIL("학생회"),
    DORMITORY_MANAGER("기숙사자치위원회"),
    GENERAL_STUDENT("일반학생");

    companion object {
        fun fromRole(role: String): StudentRole? =
            entries.find { it.value == role }
    }
}
