package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.domain.auth.entity.ApiKey
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.CreateApiKeyService
import team.themoment.datagsm.global.security.data.ApiKeyEnvironment
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.time.LocalDateTime
import java.util.UUID

@Service
class CreateApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val apiKeyEnvironment: ApiKeyEnvironment,
) : CreateApiKeyService {
    @Transactional
    override fun execute(): ApiKeyResDto {
        val student = currentUserProvider.getCurrentStudent()
        val now = LocalDateTime.now()
        val expiresAt = now.plusDays(apiKeyEnvironment.expirationDays)

        val apiKey =
            apiKeyJpaRepository
                .findByApiKeyStudent(student)
                .map {
                    it.apply {
                        apiKeyValue = UUID.randomUUID()
                        createdAt = now
                        updatedAt = now
                        this.expiresAt = expiresAt
                    }
                }.orElseGet {
                    ApiKey().apply {
                        apiKeyStudent = student
                        createdAt = now
                        updatedAt = now
                        this.expiresAt = expiresAt
                    }
                }

        val savedApiKey = apiKeyJpaRepository.save(apiKey)

        return ApiKeyResDto(apiKey = savedApiKey.apiKeyValue, expiresAt = savedApiKey.expiresAt)
    }
}
