package team.themoment.datagsm.common.domain.student

enum class Sex(
    val value: String,
) {
    MAN("남자"),
    WOMAN("여자"),
    ;

    companion object {
        fun fromSex(sex: String): Sex? = entries.find { it.value == sex }
    }
}
