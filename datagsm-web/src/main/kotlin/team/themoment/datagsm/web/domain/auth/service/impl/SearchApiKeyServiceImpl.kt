package team.themoment.datagsm.web.domain.auth.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.auth.dto.request.SearchApiKeyReqDto
import team.themoment.datagsm.common.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.common.domain.auth.dto.response.ApiKeySearchResDto
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.web.domain.auth.service.SearchApiKeyService
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class SearchApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
) : SearchApiKeyService {
    @Transactional(readOnly = true)
    override fun execute(searchReq: SearchApiKeyReqDto): ApiKeySearchResDto {
        val apiKeyPage =
            apiKeyJpaRepository.searchApiKeyWithPaging(
                id = searchReq.id,
                accountId = searchReq.accountId,
                scope = searchReq.scope,
                isExpired = searchReq.isExpired,
                isRenewable = searchReq.isRenewable,
                pageable = PageRequest.of(searchReq.page, searchReq.size),
            )

        val now = LocalDateTime.now()
        return ApiKeySearchResDto(
            totalPages = apiKeyPage.totalPages,
            totalElements = apiKeyPage.totalElements,
            apiKeys =
                apiKeyPage.content.map { entity ->
                    ApiKeyResDto(
                        id = entity.id!!,
                        apiKey = entity.maskedValue,
                        expiresAt = entity.expiresAt,
                        expiresInDays = maxOf(0L, ChronoUnit.DAYS.between(now, entity.expiresAt)),
                        scopes = entity.scopes,
                        description = entity.description,
                    )
                },
        )
    }
}
