package team.themoment.datagsm.web.domain.auth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
import team.themoment.datagsm.web.domain.auth.service.ReissueCurrentAccountApiKeyService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import team.themoment.sdk.logging.logger.logger
import java.time.LocalDateTime
import java.util.UUID

@Service
class ReissueCurrentAccountApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val apiKeyEnvironment: ApiKeyEnvironment,
) : ReissueCurrentAccountApiKeyService {
    @Transactional
    override fun execute(): ApiKeyResDto {
        val account = currentUserProvider.getCurrentAccount()

        val apiKey =
            apiKeyJpaRepository
                .findByAccount(account)
                .orElseThrow {
                    ExpectedException("API 키를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                }

        val renewalPeriodDays = apiKeyEnvironment.renewalPeriodDays
        if (!apiKey.canBeRenewed(renewalPeriodDays)) {
            apiKeyJpaRepository.delete(apiKey)
            throw ExpectedException(
                "API 키 갱신 기간이 지났습니다. 해당 API 키는 삭제되었습니다.",
                HttpStatus.GONE,
            )
        }

        val isAdmin = account.role in setOf(AccountRole.ADMIN, AccountRole.ROOT)
        val expirationDays = if (isAdmin) apiKeyEnvironment.adminExpirationDays else apiKeyEnvironment.expirationDays

        val oldValue = apiKey.value
        val now = LocalDateTime.now()
        val expiresAt = now.plusDays(expirationDays)

        apiKey.apply {
            value = UUID.randomUUID()
            updatedAt = now
            this.expiresAt = expiresAt
        }

        logger().info(
            "API Key reissued: accountId=${account.id}, " +
                "oldKey=${oldValue.toString().take(8)}****, " +
                "newKey=${apiKey.value.toString().take(8)}****, " +
                "scopes=${apiKey.scopes.joinToString(",")}",
        )

        val savedApiKey = apiKeyJpaRepository.save(apiKey)

        return ApiKeyResDto(
            id = savedApiKey.id!!,
            apiKey = savedApiKey.value.toString(),
            expiresAt = savedApiKey.expiresAt,
            scopes = savedApiKey.scopes,
            description = savedApiKey.description,
        )
    }
}
