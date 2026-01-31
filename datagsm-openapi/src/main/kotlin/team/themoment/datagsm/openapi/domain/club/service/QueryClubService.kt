package team.themoment.datagsm.openapi.domain.club.service

import team.themoment.datagsm.common.domain.club.dto.response.ClubListResDto
import team.themoment.datagsm.common.domain.club.entity.constant.ClubSortBy
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.global.constant.SortDirection

interface QueryClubService {
    fun execute(
        clubId: Long?,
        clubName: String?,
        clubType: ClubType?,
        page: Int,
        size: Int,
        includeLeaderInParticipants: Boolean = false,
        sortBy: ClubSortBy? = null,
        sortDirection: SortDirection = SortDirection.ASC,
    ): ClubListResDto
}
