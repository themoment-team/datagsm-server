package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import team.themoment.datagsm.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.QueryApiKeyService
import team.themoment.datagsm.global.security.provider.CurrentUserProvider

@Service
class QueryApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : QueryApiKeyService {
    @Transactional(readOnly = true)
    override fun execute(): ApiKeyResDto {
        val account = currentUserProvider.getCurrentAccount()

        val apiKey =
            apiKeyJpaRepository
                .findByApiKeyAccount(account)
                .orElseThrow {
                    ResponseStatusException(HttpStatus.NOT_FOUND, "API 키를 찾을 수 없습니다.")
                }

        return ApiKeyResDto(apiKey = apiKey.apiKeyValue, expiresAt = apiKey.expiresAt)
    }
}
