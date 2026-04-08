package team.themoment.datagsm.common.domain.client.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.custom.ClientJpaCustomRepository

interface ClientJpaRepository :
    JpaRepository<ClientJpaEntity, String>,
    ClientJpaCustomRepository {
    fun deleteAllByAccount(account: AccountJpaEntity)

    @Modifying
    @Query("DELETE FROM tb_client_scope WHERE scope = :scope", nativeQuery = true)
    fun removeScopeFromClients(@Param("scope") scope: String)

    @Modifying
    @Query("DELETE FROM tb_client_scope WHERE scope LIKE CONCAT(:prefix, '%')", nativeQuery = true)
    fun removeScopesByApplicationId(@Param("prefix") prefix: String)
}
