package team.themoment.datagsm.global.scheduler

import com.github.snowykte0426.peanut.butter.logging.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import team.themoment.datagsm.domain.neis.schedule.service.SyncScheduleService
import team.themoment.datagsm.global.common.retry.RetryExecutor
import java.time.LocalDate

@Component
class NeisScheduleSyncScheduler(
    private val syncScheduleService: SyncScheduleService,
) {
    @Scheduled(cron = "0 0 2 * * *")
    fun syncScheduleData() {
        val today = LocalDate.now()
        val nextYear = today.plusYears(1)

        try {
            RetryExecutor.executeWithRetry {
                syncScheduleService.execute(
                    fromDate = today,
                    toDate = nextYear,
                )
                logger().info("Successfully synced schedule data from $today to $nextYear")
            }
        } catch (e: Exception) {
            logger().error("Failed to sync schedule data after all retry attempts. Keeping existing data.", e)
        }
    }
}
