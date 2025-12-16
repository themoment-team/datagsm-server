package team.themoment.datagsm.global.scheduler

import com.github.snowykte0426.peanut.butter.logging.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import team.themoment.datagsm.domain.neis.meal.service.SyncMealService
import team.themoment.datagsm.global.common.retry.RetryExecutor
import java.time.LocalDate

@Component
class NeisMealSyncScheduler(
    private val syncMealService: SyncMealService,
) {
    @Scheduled(cron = "0 0 2 * * *")
    fun syncMealData() {
        val today = LocalDate.now()
        val nextYear = today.plusYears(1)

        try {
            RetryExecutor.executeWithRetry {
                syncMealService.execute(
                    fromDate = today,
                    toDate = nextYear,
                )
                logger().info("Successfully synced meal data from $today to $nextYear")
            }
        } catch (e: Exception) {
            logger().error("Failed to sync meal data after all retry attempts. Keeping existing data.", e)
        }
    }
}
