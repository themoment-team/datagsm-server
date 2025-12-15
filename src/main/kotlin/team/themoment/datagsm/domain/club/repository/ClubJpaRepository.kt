package team.themoment.datagsm.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.custom.ClubJpaCustomRepository

interface ClubJpaRepository :
    JpaRepository<ClubJpaEntity, Long>,
    ClubJpaCustomRepository {
    fun existsByName(clubName: String): Boolean

    fun existsByNameAndIdNot(
        clubName: String,
        clubId: Long,
    ): Boolean

    fun findAllByNameInAndType(
        clubs: List<String>,
        clubType: ClubType,
    ): List<ClubJpaEntity>

    fun findByType(type: ClubType): List<ClubJpaEntity>

    fun findAllByNameIn(names: List<String>): List<ClubJpaEntity>
}
