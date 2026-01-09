package team.themoment.datagsm.web.domain.auth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.entity.constant.ApiScope
import team.themoment.datagsm.common.domain.auth.dto.request.CreateApiKeyReqDto
import team.themoment.datagsm.common.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.common.domain.auth.entity.ApiKey
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
import team.themoment.datagsm.web.domain.auth.service.CreateCurrentAccountApiKeyService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime

@Service
class CreateCurrentAccountApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val apiKeyEnvironment: ApiKeyEnvironment,
) : CreateCurrentAccountApiKeyService {
    @Transactional
    override fun execute(reqDto: CreateApiKeyReqDto): ApiKeyResDto {
        val account = currentUserProvider.getCurrentAccount()

        if (apiKeyJpaRepository.findByAccount(account).isPresent) {
            throw ExpectedException("이미 API 키가 존재합니다.", HttpStatus.CONFLICT)
        }

        val isAdmin = account.role in setOf(AccountRole.ADMIN, AccountRole.ROOT)

        val validScopes = if (isAdmin) ApiScope.getAllScopes() else ApiScope.READ_ONLY_SCOPES
        val invalidScopes = reqDto.scopes.filter { it !in validScopes }
        if (invalidScopes.isNotEmpty()) {
            throw ExpectedException(
                if (isAdmin) {
                    "유효하지 않은 scope입니다: ${invalidScopes.joinToString(", ")}"
                } else {
                    "일반 사용자는 READ scope만 사용 가능합니다. 사용 불가능한 scope: ${invalidScopes.joinToString(", ")}"
                },
                HttpStatus.BAD_REQUEST,
            )
        }

        val now = LocalDateTime.now()
        val expirationDays = if (isAdmin) apiKeyEnvironment.adminExpirationDays else apiKeyEnvironment.expirationDays
        val expiresAt = now.plusDays(expirationDays)

        val rateLimitCapacity = apiKeyEnvironment.rateLimit.defaultCapacity
        val rateLimitRefillTokens = apiKeyEnvironment.rateLimit.defaultRefillTokens
        val rateLimitRefillDurationSeconds = apiKeyEnvironment.rateLimit.defaultRefillDurationSeconds

        val apiKey =
            ApiKey().apply {
                this.account = account
                createdAt = now
                updatedAt = now
                this.expiresAt = expiresAt
                updateScopes(reqDto.scopes)
                this.description = reqDto.description
                this.rateLimitCapacity = rateLimitCapacity
                this.rateLimitRefillTokens = rateLimitRefillTokens
                this.rateLimitRefillDurationSeconds = rateLimitRefillDurationSeconds
            }

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
