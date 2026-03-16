package team.themoment.datagsm.web.global.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
import team.themoment.datagsm.web.domain.auth.service.ExpireApiKeyService
import team.themoment.sdk.logging.logger.logger
import java.time.LocalDateTime

@Component
class ApiKeyCleanupScheduler(
    private val expireApiKeyService: ExpireApiKeyService,
    private val apiKeyEnvironment: ApiKeyEnvironment,
) {
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    fun cleanupExpiredApiKeys() {
        val cutoffDate = LocalDateTime.now().minusDays(apiKeyEnvironment.renewalPeriodDays)
        val deletedCount = expireApiKeyService.execute(cutoffDate)
        logger().info("Deleted $deletedCount expired API keys")
    }
}
