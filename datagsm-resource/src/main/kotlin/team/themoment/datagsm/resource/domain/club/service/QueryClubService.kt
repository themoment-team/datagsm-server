package team.themoment.datagsm.resource.domain.club.service

import team.themoment.datagsm.common.domain.club.ClubSortBy
import team.themoment.datagsm.common.domain.club.ClubType
import team.themoment.datagsm.common.global.constant.SortDirection
import team.themoment.datagsm.resource.domain.club.dto.response.ClubListResDto

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
