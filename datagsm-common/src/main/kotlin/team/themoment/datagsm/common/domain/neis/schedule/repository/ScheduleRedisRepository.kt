package team.themoment.datagsm.common.domain.neis.schedule.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.common.domain.neis.schedule.entity.ScheduleRedisEntity
import java.time.LocalDate

interface ScheduleRedisRepository :
    CrudRepository<ScheduleRedisEntity, String>,
    ScheduleRedisCustomRepository {
    fun findByDate(date: LocalDate): List<ScheduleRedisEntity>
}
