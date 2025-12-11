package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.auth.dto.request.CreateApiKeyReqDto
import team.themoment.datagsm.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.domain.auth.entity.ApiKey
import team.themoment.datagsm.domain.auth.entity.constant.ApiScope
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.CreateApiKeyService
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.data.ApiKeyEnvironment
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.time.LocalDateTime

@Service
class CreateApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val apiKeyEnvironment: ApiKeyEnvironment,
) : CreateApiKeyService {
    @Transactional
    override fun execute(reqDto: CreateApiKeyReqDto): ApiKeyResDto {
        val account = currentUserProvider.getCurrentAccount()

        if (apiKeyJpaRepository.findByAccount(account).isPresent) {
            throw ExpectedException("이미 API 키가 존재합니다.", HttpStatus.CONFLICT)
        }

        // 일반 사용자는 READ scope만 사용 가능
        val invalidScopes = reqDto.scopes.filter { it !in ApiScope.READ_ONLY_SCOPES }
        if (invalidScopes.isNotEmpty()) {
            throw ExpectedException(
                "일반 사용자는 READ scope만 사용 가능합니다. 사용 불가능한 scope: ${invalidScopes.joinToString(", ")}",
                HttpStatus.BAD_REQUEST,
            )
        }

        val now = LocalDateTime.now()
        val expiresAt = now.plusDays(apiKeyEnvironment.expirationDays)

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
