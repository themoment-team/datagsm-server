package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.auth.dto.request.CreateApiKeyReqDto
import team.themoment.datagsm.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.domain.auth.entity.ApiKey
import team.themoment.datagsm.domain.auth.entity.constant.ApiScope
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.CreateAdminApiKeyService
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.time.LocalDateTime

@Service
class CreateAdminApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : CreateAdminApiKeyService {
    companion object {
        private const val ADMIN_API_KEY_EXPIRATION_DAYS = 365L // 1년
    }

    @Transactional
    override fun execute(reqDto: CreateApiKeyReqDto): ApiKeyResDto {
        val account = currentUserProvider.getCurrentAccount()

        if (apiKeyJpaRepository.findByAccount(account).isPresent) {
            throw ExpectedException("이미 API 키가 존재합니다.", HttpStatus.CONFLICT)
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

        val apiKey =
            ApiKey().apply {
                this.account = account
                createdAt = now
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