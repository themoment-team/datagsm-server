package team.themoment.datagsm.domain.club.dto.response

import team.themoment.datagsm.domain.club.dto.internal.ClubDto

data class ClubListResDto(
    val totalPages: Int,
    val totalElements: Long,
    val clubs: List<ClubDto>,
)
