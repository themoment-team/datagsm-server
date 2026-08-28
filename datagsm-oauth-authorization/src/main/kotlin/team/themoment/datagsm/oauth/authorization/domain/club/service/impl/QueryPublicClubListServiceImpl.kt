package team.themoment.datagsm.oauth.authorization.domain.club.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.club.dto.response.ClubSummaryListResDto
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.oauth.authorization.domain.club.service.QueryPublicClubListService

@Service
class QueryPublicClubListServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
) : QueryPublicClubListService {
    @Transactional(readOnly = true)
    override fun execute(type: ClubType): ClubSummaryListResDto {
        val clubs =
            clubJpaRepository
                .findByType(type)
                .filter { it.status == ClubStatus.ACTIVE }
                .map { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) }

        return ClubSummaryListResDto(clubs = clubs)
    }
}
