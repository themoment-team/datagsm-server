package team.themoment.datagsm.web.domain.club.service

import team.themoment.datagsm.web.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.web.domain.club.dto.response.ClubResDto

interface ModifyClubService {
    fun execute(
        clubId: Long,
        reqDto: ClubReqDto,
    ): ClubResDto
}
