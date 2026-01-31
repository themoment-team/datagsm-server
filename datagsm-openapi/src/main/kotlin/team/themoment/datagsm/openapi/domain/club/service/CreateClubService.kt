package team.themoment.datagsm.resource.domain.club.service

import team.themoment.datagsm.common.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.common.domain.club.dto.response.ClubResDto

interface CreateClubService {
    fun execute(clubReqDto: ClubReqDto): ClubResDto
}
