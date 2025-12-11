package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.auth.dto.request.ModifyApiKeyReqDto
import team.themoment.datagsm.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.domain.auth.entity.constant.ApiScope
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.ModifyAdminApiKeyService
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.time.LocalDateTime

@Service
class ModifyAdminApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : ModifyAdminApiKeyService {
    companion object {
        private const val ADMIN_API_KEY_EXPIRATION_DAYS = 365L
        private const val ADMIN_RENEWAL_PERIOD_DAYS = 30L
    }

    @Transactional
    override fun execute(reqDto: ModifyApiKeyReqDto): ApiKeyResDto {
        val account = currentUserProvider.getCurrentAccount()

        val apiKey =
            apiKeyJpaRepository
                .findByAccount(account)
                .orElseThrow {
                    ExpectedException("API 키를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                }

        if (!apiKey.canBeRenewed(ADMIN_RENEWAL_PERIOD_DAYS)) {
            val renewalEndDate = apiKey.expiresAt.plusDays(ADMIN_RENEWAL_PERIOD_DAYS)
            if (!LocalDateTime.now().isBefore(renewalEndDate)) {
                apiKeyJpaRepository.delete(apiKey)
                throw ExpectedException(
                    "API 키 갱신 기간이 지났습니다. 해당 API 키는 삭제되었습니다.",
                    HttpStatus.GONE,
                )
            }
            throw ExpectedException(
                "API 키 갱신 기간이 아닙니다. 만료 ${ADMIN_RENEWAL_PERIOD_DAYS}일 전부터 만료 ${ADMIN_RENEWAL_PERIOD_DAYS}일 후까지만 갱신 가능합니다.",
                HttpStatus.BAD_REQUEST,
            )
        }

        // Admin은 모든 scope 사용 가능
        val validScopes = ApiScope.getAllScopes()
        val invalidScopes = reqDto.scopes.filter { it !in validScopes }
        if (invalidScopes.isNotEmpty()) {
            throw ExpectedException(
                "유효하지 않은 scope입니다: ${invalidScopes.joinToString(", ")}",
                HttpStatus.BAD_REQUEST,
            )
        }

        val now = LocalDateTime.now()
        val expiresAt = now.plusDays(ADMIN_API_KEY_EXPIRATION_DAYS)

        apiKey.apply {
            updatedAt = now
            this.expiresAt = expiresAt
            updateScopes(reqDto.scopes)
            this.description = reqDto.description
        }

        val savedApiKey = apiKeyJpaRepository.save(apiKey)

        return ApiKeyResDto(
            apiKey = savedApiKey.value,
            expiresAt = savedApiKey.expiresAt,
            scopes = savedApiKey.scopes,
            description = savedApiKey.description,
        )
    }
}
