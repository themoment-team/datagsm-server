package team.themoment.datagsm.domain.student.entity.constant

enum class Major {
    SW_DEVELOPMENT,
    SMART_IOT,
    AI,
    ;

    companion object {
        fun fromGrade(grade: Int): Major? =
            when (grade) {
                1, 2 -> SW_DEVELOPMENT
                3 -> SMART_IOT
                4 -> AI
                else -> null
            }
    }
}
