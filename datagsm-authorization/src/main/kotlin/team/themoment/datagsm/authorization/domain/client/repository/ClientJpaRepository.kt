package team.themoment.datagsm.authorization.domain.client.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.common.domain.account.AccountJpaEntity
import team.themoment.datagsm.common.domain.client.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.custom.ClientJpaCustomRepository

interface ClientJpaRepository :
    JpaRepository<ClientJpaEntity, String>,
    ClientJpaCustomRepository {
    fun findAllByAccount(account: AccountJpaEntity): List<ClientJpaEntity>
}
