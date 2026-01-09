package team.themoment.datagsm.resource.domain.auth.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.common.dto.auth.response.ApiKeyResDto
import team.themoment.datagsm.common.dto.auth.response.ApiKeySearchResDto
import team.themoment.datagsm.resource.domain.auth.service.SearchApiKeyService

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
                    ApiKeyResDto(
                        id = entity.id!!,
                        apiKey = entity.maskedValue,
                        expiresAt = entity.expiresAt,
                        scopes = entity.scopes,
                        description = entity.description,
                    )
                },
        )
    }
}
