package team.themoment.datagsm.common.domain.club.repository.custom

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubSortBy
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.global.constant.SortDirection

interface ClubJpaCustomRepository {
    fun searchClubWithPaging(
        id: Long?,
        name: String?,
        type: ClubType?,
        pageable: Pageable,
        sortBy: ClubSortBy?,
        sortDirection: SortDirection,
    ): Page<ClubJpaEntity>
}
