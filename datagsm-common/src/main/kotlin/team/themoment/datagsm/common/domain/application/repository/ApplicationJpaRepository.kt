package team.themoment.datagsm.common.domain.application.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.repository.custom.ApplicationJpaCustomRepository

interface ApplicationJpaRepository :
    JpaRepository<ApplicationJpaEntity, String>,
    ApplicationJpaCustomRepository
