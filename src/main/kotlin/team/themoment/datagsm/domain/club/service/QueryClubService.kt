package team.themoment.datagsm.domain.club.service

import team.themoment.datagsm.common.domain.club.ClubSortBy
import team.themoment.datagsm.common.domain.club.ClubType
import team.themoment.datagsm.domain.club.dto.response.ClubListResDto
import team.themoment.datagsm.global.common.constant.SortDirection

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
