package team.themoment.datagsm.common.domain.neis.schedule.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.common.domain.neis.schedule.entity.ScheduleRedisEntity
import java.time.LocalDate

interface ScheduleRedisRepository : CrudRepository<ScheduleRedisEntity, String> {
    fun findByDate(date: LocalDate): List<ScheduleRedisEntity>

    fun findByDateBetween(
        from: LocalDate,
        to: LocalDate,
    ): List<ScheduleRedisEntity>

    fun findByDateGreaterThanEqual(fromDate: LocalDate): List<ScheduleRedisEntity>

    fun findByDateLessThanEqual(toDate: LocalDate): List<ScheduleRedisEntity>
}
