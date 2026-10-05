package team.themoment.datagsm.web.domain.club.service.impl

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.club.dto.request.QueryPublicClubReqDto
import team.themoment.datagsm.common.domain.club.dto.response.PublicClubListResDto
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.web.domain.club.service.QueryPublicClubService

@Service
class QueryPublicClubServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
) : QueryPublicClubService {
    @Transactional(readOnly = true)
    override fun execute(queryReq: QueryPublicClubReqDto): PublicClubListResDto {
        val clubs =
            clubJpaRepository
                .findAll(Sort.by(Sort.Direction.ASC, "name"))
                .filter { queryReq.clubType == null || it.type == queryReq.clubType }
                .filter { queryReq.clubStatus == null || it.status == queryReq.clubStatus }

        return PublicClubListResDto(clubs = clubs.map { ClubSummaryDto.from(it) })
    }
}
