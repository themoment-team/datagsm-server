package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.auth.dto.request.ModifyApiKeyReqDto
import team.themoment.datagsm.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.domain.auth.entity.constant.ApiScope
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.ModifyApiKeyService
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.checker.ScopeChecker
import team.themoment.datagsm.global.security.data.ApiKeyEnvironment
import team.themoment.datagsm.global.security.data.RateLimitEnvironment
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.time.LocalDateTime

@Service
class ModifyApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val apiKeyEnvironment: ApiKeyEnvironment,
    private val rateLimitEnvironment: RateLimitEnvironment,
    private val scopeChecker: ScopeChecker,
) : ModifyApiKeyService {
    @Transactional
    override fun execute(reqDto: ModifyApiKeyReqDto): ApiKeyResDto {
        val account = currentUserProvider.getCurrentAccount()

        val apiKey =
            apiKeyJpaRepository
                .findByAccount(account)
                .orElseThrow {
                    ExpectedException("API 키를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                }

        val authentication = SecurityContextHolder.getContext().authentication
        val isAdmin =
            scopeChecker.hasScope(
                authentication,
                ApiScope.ADMIN_APIKEY.scope,
            )

        val renewalPeriodDays = if (isAdmin) apiKeyEnvironment.adminRenewalPeriodDays else apiKeyEnvironment.renewalPeriodDays

        if (!apiKey.canBeRenewed(renewalPeriodDays)) {
            val renewalEndDate = apiKey.expiresAt.plusDays(renewalPeriodDays)
            if (!LocalDateTime.now().isBefore(renewalEndDate)) {
                apiKeyJpaRepository.delete(apiKey)
                throw ExpectedException(
                    "API 키 갱신 기간이 지났습니다. 해당 API 키는 삭제되었습니다.",
                    HttpStatus.GONE,
                )
            }
            throw ExpectedException(
                "API 키 갱신 기간이 아닙니다. 만료 ${renewalPeriodDays}일 전부터 만료 ${renewalPeriodDays}일 후까지만 갱신 가능합니다.",
                HttpStatus.BAD_REQUEST,
            )
        }

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

        val rateLimitCapacity = reqDto.rateLimitCapacity ?: apiKey.rateLimitCapacity

        apiKey.apply {
            updatedAt = now
            this.expiresAt = expiresAt
            updateScopes(reqDto.scopes)
            this.description = reqDto.description
            this.rateLimitCapacity = rateLimitCapacity
        }

        val savedApiKey = apiKeyJpaRepository.save(apiKey)

        return ApiKeyResDto(
            apiKey = savedApiKey.value,
            expiresAt = savedApiKey.expiresAt,
            scopes = savedApiKey.scopes,
            description = savedApiKey.description,
            rateLimitCapacity = savedApiKey.rateLimitCapacity,
        )
    }
}
