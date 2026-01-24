package team.themoment.datagsm.resource.global.scheduler

import com.github.snowykte0426.peanut.butter.logging.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import team.themoment.datagsm.resource.domain.neis.schedule.service.SyncScheduleService
import team.themoment.datagsm.resource.global.util.AcademicYearCalculator

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
