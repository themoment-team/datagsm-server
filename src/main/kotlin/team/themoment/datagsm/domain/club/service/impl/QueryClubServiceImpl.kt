package team.themoment.datagsm.domain.club.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import team.themoment.datagsm.domain.club.dto.internal.ClubDto
import team.themoment.datagsm.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.club.service.QueryClubService

@Service
class QueryClubServiceImpl(
    private final val clubJpaRepository: ClubJpaRepository,
) : QueryClubService {
    override fun execute(
        clubId: Long?,
        clubName: String?,
        clubType: ClubType?,
        page: Int,
        size: Int,
    ): ClubResDto {
        val clubPage =
            clubJpaRepository.searchClubWithPaging(
                clubId = clubId,
                clubName = clubName,
                clubType = clubType,
                pageable = PageRequest.of(page, size),
            )

        return ClubResDto(
            totalPages = clubPage.totalPages,
            totalElements = clubPage.totalElements,
            clubs =
                clubPage.content.map { entity ->
                    ClubDto(
                        clubId = entity.clubId!!,
                        clubName = entity.clubName,
                        clubDescription = entity.clubDescription,
                        clubType = entity.clubType,
                    )
                },
        )
    }
}
