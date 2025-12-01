package team.themoment.datagsm.domain.club.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.club.dto.response.ClubListResDto
import team.themoment.datagsm.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.club.service.QueryClubService

@Service
@Transactional
class QueryClubServiceImpl(
    private final val clubJpaRepository: ClubJpaRepository,
) : QueryClubService {
    override fun execute(
        clubId: Long?,
        clubName: String?,
        clubType: ClubType?,
        page: Int,
        size: Int,
    ): ClubListResDto {
        val clubPage =
            clubJpaRepository.searchClubWithPaging(
                clubId = clubId,
                clubName = clubName,
                clubType = clubType,
                pageable = PageRequest.of(page, size),
            )

        return ClubListResDto(
            totalPages = clubPage.totalPages,
            totalElements = clubPage.totalElements,
            clubs =
                clubPage.content.map { entity ->
                    ClubResDto(
                        clubId = entity.id!!,
                        clubName = entity.name,
                        clubType = entity.type,
                    )
                },
        )
    }
}
