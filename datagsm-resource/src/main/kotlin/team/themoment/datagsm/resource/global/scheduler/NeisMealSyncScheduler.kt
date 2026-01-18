package team.themoment.datagsm.resource.global.scheduler

import com.github.snowykte0426.peanut.butter.logging.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import team.themoment.datagsm.resource.domain.neis.meal.service.SyncMealService
import java.time.LocalDate

@Component
class NeisMealSyncScheduler(
    private val syncMealService: SyncMealService,
) {
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    fun syncMealData() {
        val today = LocalDate.now()
        val nextYear = today.plusYears(1)

        try {
            syncMealService.execute(
                fromDate = today,
                toDate = nextYear,
            )
            logger().info("Successfully synced meal data from $today to $nextYear")
        } catch (e: Exception) {
            logger().error("Failed to sync meal data after all retry attempts. Keeping existing data.", e)
        }
    }
}
