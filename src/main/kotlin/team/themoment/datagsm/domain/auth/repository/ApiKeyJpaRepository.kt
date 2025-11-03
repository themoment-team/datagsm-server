package team.themoment.datagsm.domain.auth.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.domain.auth.entity.ApiKey
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@Repository
interface ApiKeyJpaRepository : JpaRepository<ApiKey, Long> {
    fun findByApiKeyAccount(account: AccountJpaEntity): Optional<ApiKey>

    fun findByApiKeyValue(apiKeyValue: UUID): Optional<ApiKey>

    fun deleteByApiKeyAccount(account: AccountJpaEntity)

    fun findAllByExpiresAtLessThanEqual(dateTime: LocalDateTime): List<ApiKey>
}
