package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import team.themoment.datagsm.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.ReissueApiKeyService
import team.themoment.datagsm.global.security.data.ApiKeyEnvironment
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.time.LocalDateTime
import java.util.UUID

@Service
class ReissueApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val apiKeyEnvironment: ApiKeyEnvironment,
) : ReissueApiKeyService {
    @Transactional
    override fun execute(): ApiKeyResDto {
        val student = currentUserProvider.getCurrentStudent()

        val apiKey =
            apiKeyJpaRepository
                .findByApiKeyStudent(student)
                .orElseThrow {
                    ResponseStatusException(HttpStatus.NOT_FOUND, "API 키를 찾을 수 없습니다.")
                }

        if (!apiKey.canBeRenewed(apiKeyEnvironment.renewalPeriodDays)) {
            val renewalEndDate = apiKey.expiresAt.plusDays(apiKeyEnvironment.renewalPeriodDays)
            if (!LocalDateTime.now().isBefore(renewalEndDate)) {
                apiKeyJpaRepository.delete(apiKey)
                throw ResponseStatusException(
                    HttpStatus.GONE,
                    "API 키 갱신 기간이 지났습니다. 해당 API 키는 삭제되었습니다.",
                )
            }
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "API 키 갱신 기간이 아닙니다. 만료 ${apiKeyEnvironment.renewalPeriodDays}일 전부터 만료 ${apiKeyEnvironment.renewalPeriodDays}일 후까지만 갱신 가능합니다.",
            )
        }

        val now = LocalDateTime.now()
        val expiresAt = now.plusDays(apiKeyEnvironment.expirationDays)

        apiKey.apply {
            apiKeyValue = UUID.randomUUID()
            createdAt = now
            this.expiresAt = expiresAt
        }

        val savedApiKey = apiKeyJpaRepository.save(apiKey)

        return ApiKeyResDto(apiKey = savedApiKey.apiKeyValue, expiresAt = savedApiKey.expiresAt)
    }
}