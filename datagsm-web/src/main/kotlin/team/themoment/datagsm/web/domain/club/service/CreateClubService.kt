package team.themoment.datagsm.web.domain.club.service

import team.themoment.datagsm.common.dto.club.request.ClubReqDto
import team.themoment.datagsm.common.dto.club.response.ClubResDto

interface CreateClubService {
    fun execute(clubReqDto: ClubReqDto): ClubResDto
}
