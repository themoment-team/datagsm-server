package team.themoment.datagsm.domain.club.dto.response

data class ClubListResDto(
    val totalPages: Int,
    val totalElements: Long,
    val clubs: List<ClubResDto>,
)
