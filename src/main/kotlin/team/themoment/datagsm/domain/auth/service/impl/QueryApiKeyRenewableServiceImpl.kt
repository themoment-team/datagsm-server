package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import team.themoment.datagsm.domain.auth.dto.response.ApiKeyRenewableResDto
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.QueryApiKeyRenewableService
import team.themoment.datagsm.global.security.data.ApiKeyEnvironment
import team.themoment.datagsm.global.security.provider.CurrentUserProvider

@Service
class QueryApiKeyRenewableServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val apiKeyEnvironment: ApiKeyEnvironment,
) : QueryApiKeyRenewableService {
    @Transactional(readOnly = true)
    override fun execute(): ApiKeyRenewableResDto {
        val student = currentUserProvider.getCurrentStudent()

        val apiKey =
            apiKeyJpaRepository
                .findByApiKeyStudent(student)
                .orElseThrow {
                    ResponseStatusException(HttpStatus.NOT_FOUND, "API 키를 찾을 수 없습니다.")
                }

        val renewable = apiKey.canBeRenewed(apiKeyEnvironment.renewalPeriodDays)

        return ApiKeyRenewableResDto(renewable = renewable)
    }
}
