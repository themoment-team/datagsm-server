package team.themoment.datagsm.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.repository.custom.ClubJpaCustomRepository

interface ClubJpaRepository : JpaRepository<ClubJpaEntity, Long>, ClubJpaCustomRepository {
}
