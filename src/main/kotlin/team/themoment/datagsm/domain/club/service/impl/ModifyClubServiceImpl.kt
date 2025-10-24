package team.themoment.datagsm.domain.club.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.club.service.ModifyClubService

@Service
@Transactional
class ModifyClubServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
) : ModifyClubService {
    override fun execute(
        clubId: Long,
        reqDto: ClubReqDto,
    ): ClubResDto {
        val club =
            clubJpaRepository
                .findById(clubId)
                .orElseThrow { IllegalArgumentException("동아리를 찾을 수 없습니다. clubId: $clubId") }

        if (reqDto.clubName != club.clubName) {
            if (clubJpaRepository.existsByClubNameAndClubIdNot(reqDto.clubName, clubId)) {
                throw IllegalArgumentException("이미 존재하는 동아리 이름입니다: ${reqDto.clubName}")
            }
            club.clubName = reqDto.clubName
        }

        club.clubDescription = reqDto.clubDescription
        club.clubType = reqDto.clubType

        return ClubResDto(
            clubId = club.clubId!!,
            clubName = club.clubName,
            clubDescription = club.clubDescription,
            clubType = club.clubType,
        )
    }
}
