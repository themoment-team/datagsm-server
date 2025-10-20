package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.auth.dto.ApiKeyResDto
import team.themoment.datagsm.domain.auth.entity.ApiKey
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.CreateApiKeyService
import team.themoment.datagsm.global.security.provider.CurrentUserProvider

@Service
class CreateApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : CreateApiKeyService {
    @Transactional
    override fun execute(): ApiKeyResDto {
        val student = currentUserProvider.getCurrentStudent()

        apiKeyJpaRepository.findByApiKeyStudent(student).ifPresent {
            apiKeyJpaRepository.delete(it)
        }

        val newApiKey =
            ApiKey().apply {
                apiKeyStudent = student
            }

        val savedApiKey = apiKeyJpaRepository.save(newApiKey)

        return ApiKeyResDto(apiKey = savedApiKey.apiKeyValue)
    }
}
