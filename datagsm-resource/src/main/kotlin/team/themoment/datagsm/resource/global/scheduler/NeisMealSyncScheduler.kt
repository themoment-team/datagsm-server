package team.themoment.datagsm.resource.global.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import team.themoment.datagsm.resource.domain.neis.meal.service.SyncMealService
import team.themoment.datagsm.resource.global.util.AcademicYearCalculator
import team.themoment.sdk.logging.logger.logger

@Component
class NeisMealSyncScheduler(
    private val syncMealService: SyncMealService,
) {
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    fun syncMealData() {
        val academicYearPeriod = AcademicYearCalculator.getCurrentAcademicYearPeriod()

        try {
            syncMealService.execute(
                fromDate = academicYearPeriod.startDate,
                toDate = academicYearPeriod.endDate,
            )
            logger().info(
                "Successfully synced meal data for academic year ${academicYearPeriod.academicYear} " +
                    "from ${academicYearPeriod.startDate} to ${academicYearPeriod.endDate}",
            )
        } catch (e: Exception) {
            logger().error("Failed to sync meal data after all retry attempts. Keeping existing data.", e)
        }
    }
}
