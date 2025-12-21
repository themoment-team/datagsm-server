package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.QueryApiKeyService
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.util.UUID

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
                .findByAccount(account)
                .orElseThrow {
                    ExpectedException("API 키를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                }

        return ApiKeyResDto(
            apiKey = maskApiKey(apiKey.value),
            expiresAt = apiKey.expiresAt,
            scopes = apiKey.scopes,
            description = apiKey.description,
        )
    }

    private fun maskApiKey(uuid: UUID): String {
        val uuidString = uuid.toString()
        return "${uuidString.take(8)}-****-****-****-********${uuidString.takeLast(4)}"
    }
}
