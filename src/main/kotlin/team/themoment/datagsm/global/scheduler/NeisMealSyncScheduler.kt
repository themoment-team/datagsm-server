package team.themoment.datagsm.global.scheduler

import com.github.snowykte0426.peanut.butter.logging.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import team.themoment.datagsm.domain.neis.meal.service.SyncMealService
import java.time.LocalDate

@Component
class NeisMealSyncScheduler(
    private val syncMealService: SyncMealService,
) {
    @Scheduled(cron = "0 0 2 * * *")
    fun syncMealData() {
        val today = LocalDate.now()
        val nextYear = today.plusYears(1)

        var retryCount = 0
        val maxRetries = 3

        while (retryCount < maxRetries) {
            try {
                syncMealService.execute(
                    fromDate = today,
                    toDate = nextYear,
                )
                logger().info("Successfully synced meal data from $today to $nextYear")
                return
            } catch (e: Exception) {
                retryCount++
                logger().error("Failed to sync meal data (attempt $retryCount/$maxRetries)", e)
                if (retryCount < maxRetries) {
                    Thread.sleep(5000L * retryCount)
                }
            }
        }

        logger().error("Failed to sync meal data after $maxRetries attempts. Keeping existing data.")
    }
}
