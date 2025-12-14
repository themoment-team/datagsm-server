package team.themoment.datagsm.domain.client.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.domain.client.repository.custom.ClientJpaCustomRepository

interface ClientJpaRepository :
    JpaRepository<ClientJpaEntity, String>,
    ClientJpaCustomRepository {
    fun findAllByAccount(account: AccountJpaEntity): List<ClientJpaEntity>
}
