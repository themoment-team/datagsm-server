package team.themoment.datagsm.domain.club.service

import team.themoment.datagsm.domain.club.dto.internal.ClubDto
import team.themoment.datagsm.domain.club.dto.request.ClubReqDto

interface CreateClubService {
    fun execute(clubReqDto: ClubReqDto): ClubDto
}
