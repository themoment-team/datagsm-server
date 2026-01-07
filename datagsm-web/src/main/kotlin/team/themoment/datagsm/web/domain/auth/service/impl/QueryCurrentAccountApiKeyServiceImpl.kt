package team.themoment.datagsm.web.domain.auth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.web.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.web.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.web.domain.auth.service.QueryCurrentAccountApiKeyService
import team.themoment.datagsm.web.global.exception.error.ExpectedException
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider

@Service
class QueryCurrentAccountApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : QueryCurrentAccountApiKeyService {
    @Transactional(readOnly = true)
    override fun execute(): ApiKeyResDto {
        val account = currentUserProvider.getCurrentAccount()

        val apiKey =
            apiKeyJpaRepository
                .findByAccount(account)
                .orElseThrow {
                    ExpectedException("API 키를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                }

        return ApiKeyResDto(
            id = apiKey.id!!,
            apiKey = apiKey.maskedValue,
            expiresAt = apiKey.expiresAt,
            scopes = apiKey.scopes,
            description = apiKey.description,
        )
    }
}
