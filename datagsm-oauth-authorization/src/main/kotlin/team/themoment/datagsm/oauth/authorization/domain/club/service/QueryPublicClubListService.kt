package team.themoment.datagsm.oauth.authorization.domain.club.service

import team.themoment.datagsm.common.domain.club.dto.response.ClubSummaryListResDto
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType

interface QueryPublicClubListService {
    fun execute(type: ClubType): ClubSummaryListResDto
}
