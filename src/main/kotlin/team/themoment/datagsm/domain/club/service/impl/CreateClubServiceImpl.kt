package team.themoment.datagsm.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.club.service.CreateClubService
import team.themoment.datagsm.global.exception.error.ExpectedException

@Service
@Transactional
class CreateClubServiceImpl(
    private final val clubJpaRepository: ClubJpaRepository,
) : CreateClubService {
    override fun execute(clubReqDto: ClubReqDto): ClubResDto {
        if (clubJpaRepository.existsByClubName(clubReqDto.clubName)) {
            throw ExpectedException("이미 존재하는 동아리 이름입니다: ${clubReqDto.clubName}", HttpStatus.CONFLICT)
        }

        val clubEntity =
            ClubJpaEntity().apply {
                name = clubReqDto.clubName
                type = clubReqDto.clubType
            }
        val savedClubEntity = clubJpaRepository.save(clubEntity)

        return ClubResDto(
            clubId = savedClubEntity.id!!,
            clubName = savedClubEntity.name,
            clubType = savedClubEntity.type,
        )
    }
}
