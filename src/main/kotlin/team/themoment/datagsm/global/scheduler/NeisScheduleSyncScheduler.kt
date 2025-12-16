package team.themoment.datagsm.global.scheduler

import com.github.snowykte0426.peanut.butter.logging.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import team.themoment.datagsm.domain.neis.schedule.service.SyncScheduleService
import java.time.LocalDate

@Component
class NeisScheduleSyncScheduler(
    private val syncScheduleService: SyncScheduleService,
) {
    @Scheduled(cron = "0 0 2 * * *")
    fun syncScheduleData() {
        val today = LocalDate.now()
        val nextYear = today.plusYears(1)

        var retryCount = 0
        val maxRetries = 3

        while (retryCount < maxRetries) {
            try {
                syncScheduleService.execute(
                    fromDate = today,
                    toDate = nextYear,
                )
                logger().info("Successfully synced schedule data from $today to $nextYear")
                return
            } catch (e: Exception) {
                retryCount++
                logger().error("Failed to sync schedule data (attempt $retryCount/$maxRetries)", e)
                if (retryCount < maxRetries) {
                    Thread.sleep(5000L * retryCount)
                }
            }
        }

        logger().error("Failed to sync schedule data after $maxRetries attempts. Keeping existing data.")
    }
}
