package team.themoment.datagsm.domain.club.service

import team.themoment.datagsm.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.domain.club.dto.request.ClubReqDto

interface CreateClubService {
    fun execute(clubReqDto: ClubReqDto): ClubResDto
}
