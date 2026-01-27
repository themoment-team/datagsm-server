package team.themoment.datagsm.common.domain.student.entity.constant

/**
 * 인증/인가와 관련 없이 학생의 역할을 정의하는 열거형 클래스입니다.
 */
enum class StudentRole(
    val value: String,
) {
    STUDENT_COUNCIL("학생회"),
    DORMITORY_MANAGER("기숙사자치위원회"),
    GENERAL_STUDENT("일반학생"),
    GRADUATE("졸업생"),
    ;

    companion object {
        fun fromRole(role: String): StudentRole? = entries.find { it.value == role }
    }
}
