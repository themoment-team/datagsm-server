package team.themoment.datagsm.web.global.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import team.themoment.datagsm.web.global.redis.ExpiredRedisEntryCleaner
import team.themoment.sdk.logging.logger.logger

@Component
class ExpiredRedisEntryCleanupScheduler(
    private val expiredRedisEntryCleaner: ExpiredRedisEntryCleaner,
) {
    @Scheduled(cron = "0 30 3 * * *", zone = "Asia/Seoul")
    fun cleanUpExpiredRedisEntries() {
        expiredRedisEntryCleaner.cleanUp().forEach { (keyspace, removedCount) ->
            logger().info("Removed {} expired entries from keyspace {}", removedCount, keyspace)
        }
    }
}
