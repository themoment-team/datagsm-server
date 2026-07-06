package team.themoment.datagsm.common.domain.event.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.event.entity.EventJpaEntity
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.entity.constant.EventVerificationStatus

@Repository
interface EventJpaRepository : JpaRepository<EventJpaEntity, Long> {
    fun findAllByAccount(account: AccountJpaEntity): List<EventJpaEntity>

    fun findByIdAndAccount(
        id: Long,
        account: AccountJpaEntity,
    ): EventJpaEntity?

    fun countByAccount(account: AccountJpaEntity): Long

    fun findAllByEventsContainsAndIsActiveTrueAndVerificationStatus(
        event: EventType,
        verificationStatus: EventVerificationStatus,
    ): List<EventJpaEntity>
}
