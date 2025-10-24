package team.themoment.datagsm.domain.club.service

import team.themoment.datagsm.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.domain.club.dto.response.ClubResDto

interface ModifyClubService {
    fun execute(
        clubId: Long,
        reqDto: ClubReqDto,
    ): ClubResDto
}
