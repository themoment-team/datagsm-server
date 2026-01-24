package team.themoment.datagsm.web.domain.auth.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.web.domain.auth.service.DeleteApiKeyByIdService
import team.themoment.sdk.exception.ExpectedException

@Service
class DeleteApiKeyByIdServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
) : DeleteApiKeyByIdService {
    @Transactional
    override fun execute(apiKeyId: Long) {
        val apiKey =
            apiKeyJpaRepository.findByIdOrNull(apiKeyId)
                ?: throw ExpectedException("API 키를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        apiKeyJpaRepository.delete(apiKey)
    }
}
