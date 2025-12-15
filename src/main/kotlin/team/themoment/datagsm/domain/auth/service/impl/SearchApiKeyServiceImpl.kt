package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.auth.dto.response.ApiKeySearchResDto
import team.themoment.datagsm.domain.auth.dto.response.MaskedApiKeyResDto
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.SearchApiKeyService
import java.util.UUID

@Service
@Transactional(readOnly = true)
class SearchApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
) : SearchApiKeyService {
    override fun execute(
        id: Long?,
        accountId: Long?,
        scope: String?,
        isExpired: Boolean?,
        isRenewable: Boolean?,
        page: Int,
        size: Int,
    ): ApiKeySearchResDto {
        val apiKeyPage =
            apiKeyJpaRepository.searchApiKeyWithPaging(
                id = id,
                accountId = accountId,
                scope = scope,
                isExpired = isExpired,
                isRenewable = isRenewable,
                pageable = PageRequest.of(page, size),
            )

        return ApiKeySearchResDto(
            totalPages = apiKeyPage.totalPages,
            totalElements = apiKeyPage.totalElements,
            apiKeys =
                apiKeyPage.content.map { entity ->
                    MaskedApiKeyResDto(
                        apiKey = maskApiKey(entity.value),
                        expiresAt = entity.expiresAt,
                        scopes = entity.scopes,
                        description = entity.description,
                    )
                },
        )
    }

    private fun maskApiKey(uuid: UUID): String {
        val uuidString = uuid.toString()
        return "${uuidString.take(8)}-****-****-****-********${uuidString.takeLast(4)}"
    }
}
