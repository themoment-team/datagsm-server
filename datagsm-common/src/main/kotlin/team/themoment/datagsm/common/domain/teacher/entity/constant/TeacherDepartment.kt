package team.themoment.datagsm.common.domain.teacher.entity.constant

/**
 * 선생님의 소속 부서를 정의하는 열거형 클래스입니다.
 */
enum class TeacherDepartment(
    val value: String,
) {
    MEISTER("마이스터부"),
    DORMITORY("사감선생님"),
    GRADE("학년부"),
    ACADEMIC_AFFAIRS("교무부"),
    PROFESSIONAL_EDUCATION("전문교육부"),
    EMPLOYMENT_CAREER("취업진로부"),
    ADMINISTRATION("행정실"),
    ;

    companion object {
        fun fromDepartment(department: String): TeacherDepartment? = entries.find { it.value == department }
    }
}
