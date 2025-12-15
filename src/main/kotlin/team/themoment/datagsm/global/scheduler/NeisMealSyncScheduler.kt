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
    @Scheduled(cron = "0 0 2 * * MON")
    fun syncMealData() {
        val today = LocalDate.now()
        val nextMonth = today.plusMonths(1)

        try {
            syncMealService.execute(
                fromDate = today,
                toDate = nextMonth,
            )
            logger().info("Successfully synced meal data from $today to $nextMonth")
        } catch (e: Exception) {
            logger().error("Failed to sync meal data", e)
        }
    }
}
