package team.themoment.datagsm.global.scheduler

import com.github.snowykte0426.peanut.butter.logging.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.global.security.data.ApiKeyEnvironment
import java.time.LocalDateTime

@Component
class ApiKeyCleanupScheduler(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val apiKeyEnvironment: ApiKeyEnvironment,
) {
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    fun cleanupExpiredApiKeys() {
        val cutoffDate = LocalDateTime.now().minusDays(apiKeyEnvironment.renewalPeriodDays)
        val expiredKeys = apiKeyJpaRepository.findAllByExpiresAtBefore(cutoffDate)

        if (expiredKeys.isNotEmpty()) {
            apiKeyJpaRepository.deleteAll(expiredKeys)
            logger().info("Deleted ${expiredKeys.size} expired API keys")
        }
    }
}
