package team.themoment.datagsm.web.domain.auth.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
import team.themoment.datagsm.web.domain.auth.service.ExtendApiKeyByIdService
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class ExtendApiKeyByIdServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val apiKeyEnvironment: ApiKeyEnvironment,
) : ExtendApiKeyByIdService {
    @Transactional
    override fun execute(apiKeyId: Long): ApiKeyResDto {
        val apiKey =
            apiKeyJpaRepository.findByIdOrNull(apiKeyId)
                ?: throw ExpectedException("API 키를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        if (apiKey.isExpired()) {
            throw ExpectedException("만료된 API 키는 연장할 수 없습니다.", HttpStatus.BAD_REQUEST)
        }
        val now = LocalDateTime.now()
        apiKey.expiresAt = now.plusDays(apiKeyEnvironment.adminExpirationDays)
        return ApiKeyResDto(
            id = apiKey.id!!,
            apiKey = apiKey.maskedValue,
            expiresAt = apiKey.expiresAt,
            expiresInDays = maxOf(0L, ChronoUnit.DAYS.between(now, apiKey.expiresAt)),
            scopes = apiKey.scopes,
            description = apiKey.description,
        )
    }
}
