package team.themoment.datagsm.openapi.global.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import team.themoment.datagsm.openapi.domain.neis.timetable.service.SyncTimetableService
import team.themoment.datagsm.openapi.global.util.AcademicYearCalculator
import team.themoment.sdk.logging.logger.logger

@Component
class NeisTimetableSyncScheduler(
    private val syncTimetableService: SyncTimetableService,
) {
    @Scheduled(cron = "0 20 2 * * *", zone = "Asia/Seoul")
    fun syncTimetableData() {
        val academicYearPeriod = AcademicYearCalculator.getCurrentAcademicYearPeriod()

        try {
            syncTimetableService.execute(
                fromDate = academicYearPeriod.startDate,
                toDate = academicYearPeriod.endDate,
            )
            logger().info(
                "Successfully synced timetable data for academic year {}",
                academicYearPeriod.academicYear,
            )
        } catch (e: Exception) {
            logger().error("Failed to sync timetable data after all retry attempts. Keeping existing data.", e)
        }
    }
}
