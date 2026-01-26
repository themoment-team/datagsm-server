package team.themoment.datagsm.web.global.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
import team.themoment.sdk.logging.logger.logger
import java.time.LocalDateTime

@Component
class ApiKeyCleanupScheduler(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val apiKeyEnvironment: ApiKeyEnvironment,
) {
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    fun cleanupExpiredApiKeys() {
        val cutoffDate = LocalDateTime.now().minusDays(apiKeyEnvironment.renewalPeriodDays)
        val expiredKeys = apiKeyJpaRepository.findAllByExpiresAtLessThanEqual(cutoffDate)

        if (expiredKeys.isNotEmpty()) {
            apiKeyJpaRepository.deleteAll(expiredKeys)
            logger().info("Deleted ${expiredKeys.size} expired API keys")
        }
    }
}
