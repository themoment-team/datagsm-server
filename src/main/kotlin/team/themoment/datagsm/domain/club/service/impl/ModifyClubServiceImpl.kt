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

        if (reqDto.name != club.name) {
            if (clubJpaRepository.existsByClubNameAndClubIdNot(reqDto.name, clubId)) {
                throw ExpectedException("이미 존재하는 동아리 이름입니다: ${reqDto.name}", HttpStatus.CONFLICT)
            }
            club.name = reqDto.name
        }

        club.type = reqDto.type

        return ClubResDto(
            id = club.id!!,
            name = club.name,
            type = club.type,
        )
    }
}
