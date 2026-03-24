package team.themoment.datagsm.web.domain.auth.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.auth.dto.request.ExtendApiKeyReqDto
import team.themoment.datagsm.common.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
import team.themoment.datagsm.web.domain.auth.service.ExtendApiKeyByIdService
import team.themoment.sdk.exception.ExpectedException
import team.themoment.sdk.logging.logger.logger
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class ExtendApiKeyByIdServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val apiKeyEnvironment: ApiKeyEnvironment,
) : ExtendApiKeyByIdService {
    @Transactional(noRollbackFor = [ExpectedException::class])
    override fun execute(
        apiKeyId: Long,
        reqDto: ExtendApiKeyReqDto,
    ): ApiKeyResDto {
        val apiKey =
            apiKeyJpaRepository.findByIdOrNull(apiKeyId)
                ?: throw ExpectedException("API 키를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        val renewalPeriodDays = apiKeyEnvironment.renewalPeriodDays
        if (!apiKey.canBeRenewed(renewalPeriodDays)) {
            apiKeyJpaRepository.delete(apiKey)
            throw ExpectedException("API 키 갱신 기간이 지났습니다. 해당 API 키는 삭제되었습니다.", HttpStatus.GONE)
        }

        val now = LocalDateTime.now()
        apiKey.expiresAt = now.plusDays(reqDto.days)
        logger().info("Extended API key expiration for apiKeyId {} by {} days", apiKeyId, reqDto.days)
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
