package team.themoment.datagsm.domain.neis.schedule.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.domain.neis.schedule.entity.ScheduleRedisEntity

interface ScheduleRedisRepository : CrudRepository<ScheduleRedisEntity, String>
