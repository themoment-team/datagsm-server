package team.themoment.datagsm.common.domain.neis.timetable.repository

import team.themoment.datagsm.common.domain.neis.timetable.entity.TimetableRedisEntity
import java.time.LocalDate

interface TimetableRedisCustomRepository {
    fun findByGradeAndClassNumAndDate(
        grade: Int,
        classNum: Int,
        date: LocalDate,
    ): List<TimetableRedisEntity>

    fun findByGradeAndClassNumAndDateBetween(
        grade: Int,
        classNum: Int,
        from: LocalDate,
        to: LocalDate,
    ): List<TimetableRedisEntity>

    fun findByGradeAndClassNumAndDateGreaterThanEqual(
        grade: Int,
        classNum: Int,
        fromDate: LocalDate,
    ): List<TimetableRedisEntity>

    fun findByGradeAndClassNumAndDateLessThanEqual(
        grade: Int,
        classNum: Int,
        toDate: LocalDate,
    ): List<TimetableRedisEntity>
}
