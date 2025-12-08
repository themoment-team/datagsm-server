package team.themoment.datagsm.domain.student.entity.constant

enum class Sex(val value: String) {
    MAN("남자"),
    WOMAN("여자");

    companion object {
        fun fromSex(sex: String): Sex? =
            when (sex) {
                "남자" -> MAN
                "여자" -> WOMAN
                else -> null
            }
    }
}
