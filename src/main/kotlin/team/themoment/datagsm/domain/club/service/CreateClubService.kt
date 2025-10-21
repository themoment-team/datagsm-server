package team.themoment.datagsm.domain.club.service

import team.themoment.datagsm.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.domain.club.dto.response.ClubResDto

interface CreateClubService {
    fun execute(clubReqDto: ClubReqDto): ClubResDto
}
