package team.themoment.datagsm.domain.club.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.domain.club.dto.internal.ClubDto
import team.themoment.datagsm.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.club.service.CreateClubService

@Service
class CreateClubServiceImpl(
    private final val clubJpaRepository: ClubJpaRepository,
) : CreateClubService {
    override fun execute(clubReqDto: ClubReqDto): ClubResDto {
        if (clubJpaRepository.existsByClubName(clubReqDto.clubName)) {
            throw IllegalArgumentException("이미 존재하는 동아리 이름입니다: ${clubReqDto.clubName}")
        }

        val clubEntity =
            ClubJpaEntity().apply {
                clubName = clubReqDto.clubName
                clubDescription = clubReqDto.clubDescription
                clubType = clubReqDto.clubType
            }
        val savedClubEntity = clubJpaRepository.save(clubEntity)

        val clubDto =
            ClubDto(
                clubId = savedClubEntity.clubId!!,
                clubName = savedClubEntity.clubName,
                clubDescription = savedClubEntity.clubDescription,
                clubType = savedClubEntity.clubType,
            )

        return ClubResDto(
            totalPages = 1,
            totalElements = 1L,
            clubs = listOf(clubDto),
        )
    }
}
