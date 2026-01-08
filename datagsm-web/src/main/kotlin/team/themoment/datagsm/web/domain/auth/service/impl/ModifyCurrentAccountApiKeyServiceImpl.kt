package team.themoment.datagsm.web.domain.auth.service.impl

import com.github.snowykte0426.peanut.butter.logging.logger
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.ApiScope
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.web.domain.auth.dto.request.ModifyApiKeyReqDto
import team.themoment.datagsm.web.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.web.domain.auth.service.ModifyCurrentAccountApiKeyService
import team.themoment.datagsm.web.global.security.checker.ScopeChecker
import team.themoment.datagsm.web.global.security.data.ApiKeyEnvironment
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime
import java.util.UUID

@Service
class ModifyCurrentAccountApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val apiKeyEnvironment: ApiKeyEnvironment,
    private val scopeChecker: ScopeChecker,
) : ModifyCurrentAccountApiKeyService {
    @Transactional
    override fun execute(reqDto: ModifyApiKeyReqDto): ApiKeyResDto {
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

        val authentication = SecurityContextHolder.getContext().authentication
        val isAdmin =
            scopeChecker.hasScope(
                authentication!!,
                ApiScope.ADMIN_APIKEY.scope,
            )
        val validScopes = if (isAdmin) ApiScope.getAllScopes() else ApiScope.READ_ONLY_SCOPES
        val invalidScopes = reqDto.scopes.filter { it !in validScopes }
        if (invalidScopes.isNotEmpty()) {
            logger().warn(
                "Invalid scopes attempted: user=${account.email}, isAdmin=$isAdmin, " +
                    "invalidScopes=${invalidScopes.joinToString(", ")}",
            )
            throw ExpectedException(
                "요청한 권한 범위가 유효하지 않습니다.",
                HttpStatus.BAD_REQUEST,
            )
        }
        val now = LocalDateTime.now()
        val expirationDays = if (isAdmin) apiKeyEnvironment.adminExpirationDays else apiKeyEnvironment.expirationDays
        val expiresAt = now.plusDays(expirationDays)
        val isScopeChanged = apiKey.scopes != reqDto.scopes
        val isDescriptionChanged = apiKey.description != reqDto.description
        val oldValue = apiKey.value
        val isReissued = isScopeChanged || isDescriptionChanged
        apiKey.apply {
            updatedAt = now
            this.expiresAt = expiresAt
            this.description = reqDto.description
            if (isReissued) {
                value = UUID.randomUUID()
                if (isScopeChanged) {
                    updateScopes(reqDto.scopes)
                }
                logger().info(
                    "API Key reissued: accountId=${account.id}, " +
                        "oldKey=${oldValue.toString().take(8)}****, newKey=${value.toString().take(8)}****, " +
                        "scopeChanged=$isScopeChanged, descriptionChanged=$isDescriptionChanged",
                )
            } else {
                logger().info(
                    "API Key renewed: accountId=${account.id}, " +
                        "key=${value.toString().take(8)}****, expiresAt=$expiresAt",
                )
            }
        }
        val savedApiKey = apiKeyJpaRepository.save(apiKey)
        return ApiKeyResDto(
            id = savedApiKey.id!!,
            apiKey = if (isReissued) savedApiKey.value.toString() else savedApiKey.maskedValue,
            expiresAt = savedApiKey.expiresAt,
            scopes = savedApiKey.scopes,
            description = savedApiKey.description,
        )
    }
}
