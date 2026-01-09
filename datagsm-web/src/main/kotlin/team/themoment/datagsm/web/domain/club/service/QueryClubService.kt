package team.themoment.datagsm.web.domain.club.service

import team.themoment.datagsm.common.domain.club.entity.constant.ClubSortBy
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.dto.club.response.ClubListResDto
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
