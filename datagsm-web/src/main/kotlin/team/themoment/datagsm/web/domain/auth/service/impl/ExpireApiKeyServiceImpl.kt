package team.themoment.datagsm.web.domain.auth.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.web.domain.auth.service.ExpireApiKeyService
import java.time.LocalDateTime

@Service
class ExpireApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
) : ExpireApiKeyService {
    @Transactional
    override fun execute(cutoffDate: LocalDateTime): Long = apiKeyJpaRepository.deleteExpiredKeys(cutoffDate)
}
