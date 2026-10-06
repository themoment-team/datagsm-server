package team.themoment.datagsm.web.domain.club.service

import team.themoment.datagsm.common.domain.club.dto.request.QueryPublicClubReqDto
import team.themoment.datagsm.common.domain.club.dto.response.PublicClubListResDto

interface QueryPublicClubService {
    fun execute(queryReq: QueryPublicClubReqDto): PublicClubListResDto
}
