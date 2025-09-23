package team.themoment.datagsm.domain.club.repository.custom

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.entity.constant.ClubType

interface ClubJpaCustomRepository {
    fun searchClubWithPaging(
        clubId: Long?,
        clubName: String?,
        clubType: ClubType?,
        pageable: Pageable
    ): Page<ClubJpaEntity>
}
