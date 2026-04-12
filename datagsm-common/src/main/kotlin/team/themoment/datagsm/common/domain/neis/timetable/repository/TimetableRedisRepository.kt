package team.themoment.datagsm.common.domain.neis.timetable.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.common.domain.neis.timetable.entity.TimetableRedisEntity

interface TimetableRedisRepository :
    CrudRepository<TimetableRedisEntity, String>,
    TimetableRedisCustomRepository
