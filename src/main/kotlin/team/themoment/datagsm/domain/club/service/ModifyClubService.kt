package team.themoment.datagsm.domain.club.service

import team.themoment.datagsm.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.domain.club.dto.response.ClubListResDto

interface ModifyClubService {
    fun execute(
        clubId: Long,
        reqDto: ClubReqDto,
    ): ClubListResDto
}
