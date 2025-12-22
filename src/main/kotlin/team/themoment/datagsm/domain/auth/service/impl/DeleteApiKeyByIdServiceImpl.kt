package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.DeleteApiKeyByIdService
import team.themoment.datagsm.global.exception.error.ExpectedException

@Service
class DeleteApiKeyByIdServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
) : DeleteApiKeyByIdService {
    @Transactional
    override fun execute(apiKeyId: Long) {
        val apiKey =
            apiKeyJpaRepository
                .findById(apiKeyId)
                .orElseThrow {
                    ExpectedException("API 키를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                }

        apiKeyJpaRepository.delete(apiKey)
    }
}