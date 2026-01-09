package team.themoment.datagsm.common.domain.auth.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.auth.entity.ApiKey
import team.themoment.datagsm.common.domain.auth.repository.custom.ApiKeyJpaCustomRepository
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@Repository
interface ApiKeyJpaRepository :
    JpaRepository<ApiKey, Long>,
    ApiKeyJpaCustomRepository {
    fun findByAccount(account: AccountJpaEntity): Optional<ApiKey>

    fun findByValue(apiKeyValue: UUID): Optional<ApiKey>

    fun deleteByAccount(account: AccountJpaEntity)

    fun findAllByExpiresAtLessThanEqual(dateTime: LocalDateTime): List<ApiKey>
}
