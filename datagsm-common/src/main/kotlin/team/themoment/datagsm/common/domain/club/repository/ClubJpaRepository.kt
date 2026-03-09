package team.themoment.datagsm.common.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.custom.ClubJpaCustomRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity

interface ClubJpaRepository :
    JpaRepository<ClubJpaEntity, Long>,
    ClubJpaCustomRepository {
    fun findAllByLeader(leader: StudentJpaEntity): List<ClubJpaEntity>

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

    fun findByNameNotIn(names: Collection<String>): List<ClubJpaEntity>
}
