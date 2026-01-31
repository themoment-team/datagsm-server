package team.themoment.datagsm.openapi.global.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import team.themoment.datagsm.openapi.domain.neis.schedule.service.SyncScheduleService
import team.themoment.datagsm.openapi.global.util.AcademicYearCalculator
import team.themoment.sdk.logging.logger.logger

@Component
class NeisScheduleSyncScheduler(
    private val syncScheduleService: SyncScheduleService,
) {
    @Scheduled(cron = "0 10 2 * * *", zone = "Asia/Seoul")
    fun syncScheduleData() {
        val academicYearPeriod = AcademicYearCalculator.getCurrentAcademicYearPeriod()

        try {
            syncScheduleService.execute(
                fromDate = academicYearPeriod.startDate,
                toDate = academicYearPeriod.endDate,
            )
            logger().info(
                "Successfully synced schedule data for academic year ${academicYearPeriod.academicYear} " +
                    "from ${academicYearPeriod.startDate} to ${academicYearPeriod.endDate}",
            )
        } catch (e: Exception) {
            logger().error("Failed to sync schedule data after all retry attempts. Keeping existing data.", e)
        }
    }
}
