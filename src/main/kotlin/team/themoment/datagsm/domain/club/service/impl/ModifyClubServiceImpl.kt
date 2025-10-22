package team.themoment.datagsm.domain.club.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.club.dto.internal.ClubDto
import team.themoment.datagsm.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.domain.club.dto.response.ClubListResDto
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
    ): ClubListResDto {
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

        val saved = clubJpaRepository.save(club)

        val dto =
            ClubDto(
                clubId = saved.clubId!!,
                clubName = saved.clubName,
                clubDescription = saved.clubDescription,
                clubType = saved.clubType,
            )

        return ClubListResDto(
            totalPages = 1,
            totalElements = 1L,
            clubs = listOf(dto),
        )
    }
}
