package team.themoment.datagsm.common.domain.club

enum class ClubType(
    val value: String,
) {
    MAJOR_CLUB("전공동아리"),
    JOB_CLUB("취업동아리"),
    AUTONOMOUS_CLUB("창체동아리"),
    ;

    companion object {
        fun fromClubType(clubType: String): ClubType? = entries.find { it.value == clubType }
    }
}
