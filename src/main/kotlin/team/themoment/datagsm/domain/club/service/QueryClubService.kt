package team.themoment.datagsm.domain.club.service

import team.themoment.datagsm.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.domain.club.entity.constant.ClubType

interface QueryClubService {
    fun execute(
        clubId: Long?,
        clubName: String?,
        clubType: ClubType?,
        page: Int,
        size: Int,
    ): ClubResDto
}
