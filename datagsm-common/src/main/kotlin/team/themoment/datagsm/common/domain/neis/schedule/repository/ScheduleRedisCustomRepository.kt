package team.themoment.datagsm.common.domain.neis.schedule.repository

import team.themoment.datagsm.common.domain.neis.schedule.entity.ScheduleRedisEntity
import java.time.LocalDate

interface ScheduleRedisCustomRepository {
    fun findByDateBetween(
        from: LocalDate,
        to: LocalDate,
    ): List<ScheduleRedisEntity>

    fun findByDateGreaterThanEqual(fromDate: LocalDate): List<ScheduleRedisEntity>

    fun findByDateLessThanEqual(toDate: LocalDate): List<ScheduleRedisEntity>
}
