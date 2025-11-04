package team.themoment.datagsm.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.club.service.ModifyClubService
import team.themoment.datagsm.global.exception.error.ExpectedException

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
                .orElseThrow { ExpectedException("동아리를 찾을 수 없습니다. clubId: $clubId", HttpStatus.NOT_FOUND) }

        if (reqDto.clubName != club.clubName) {
            if (clubJpaRepository.existsByClubNameAndClubIdNot(reqDto.clubName, clubId)) {
                throw ExpectedException("이미 존재하는 동아리 이름입니다: ${reqDto.clubName}", HttpStatus.CONFLICT)
            }
            club.clubName = reqDto.clubName
        }

        club.clubType = reqDto.clubType

        return ClubResDto(
            clubId = club.clubId!!,
            clubName = club.clubName,
            clubType = club.clubType,
        )
    }
}
