package team.themoment.datagsm.web.domain.club.service

import team.themoment.datagsm.web.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.web.domain.club.dto.response.ClubResDto

interface CreateClubService {
    fun execute(clubReqDto: ClubReqDto): ClubResDto
}
