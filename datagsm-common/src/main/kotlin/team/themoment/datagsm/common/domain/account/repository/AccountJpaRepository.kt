package team.themoment.datagsm.common.domain.account.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.repository.custom.AccountJpaCustomRepository
import java.util.Optional

@Repository
interface AccountJpaRepository :
    JpaRepository<AccountJpaEntity, Long>,
    AccountJpaCustomRepository {
    fun findByEmail(email: String): Optional<AccountJpaEntity>

    fun findAllByObjectTypeAndObjectIdIn(
        objectType: AccountObjectType,
        objectIds: Collection<Long>,
    ): List<AccountJpaEntity>
}
