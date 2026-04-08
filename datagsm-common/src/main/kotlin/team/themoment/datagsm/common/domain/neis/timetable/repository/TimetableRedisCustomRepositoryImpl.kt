package team.themoment.datagsm.common.domain.neis.timetable.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.neis.timetable.entity.TimetableRedisEntity
import java.time.LocalDate

@Repository
class TimetableRedisCustomRepositoryImpl(
    private val redisTemplate: RedisTemplate<String, String>,
) : TimetableRedisCustomRepository {
    override fun findByGradeAndClassNumAndDate(
        grade: Int,
        classNum: Int,
        date: LocalDate,
    ): List<TimetableRedisEntity> {
        val ids = redisTemplate.opsForSet().members("timetable:date:$date") ?: emptySet()
        return findEntitiesByIds(ids).filter { it.grade == grade && it.classNum == classNum }
    }

    override fun findByGradeAndClassNumAndDateBetween(
        grade: Int,
        classNum: Int,
        from: LocalDate,
        to: LocalDate,
    ): List<TimetableRedisEntity> {
        val ids = collectIdsByDateRange(from, to)
        return findEntitiesByIds(ids).filter { it.grade == grade && it.classNum == classNum }
    }

    override fun findByGradeAndClassNumAndDateGreaterThanEqual(
        grade: Int,
        classNum: Int,
        fromDate: LocalDate,
    ): List<TimetableRedisEntity> {
        val toDate = fromDate.plusYears(1)
        val ids = collectIdsByDateRange(fromDate, toDate)
        return findEntitiesByIds(ids).filter { it.grade == grade && it.classNum == classNum }
    }

    override fun findByGradeAndClassNumAndDateLessThanEqual(
        grade: Int,
        classNum: Int,
        toDate: LocalDate,
    ): List<TimetableRedisEntity> {
        val fromDate = toDate.minusYears(1)
        val ids = collectIdsByDateRange(fromDate, toDate)
        return findEntitiesByIds(ids).filter { it.grade == grade && it.classNum == classNum }
    }

    private fun collectIdsByDateRange(
        from: LocalDate,
        to: LocalDate,
    ): Set<String> {
        val ids = mutableSetOf<String>()
        var currentDate = from

        while (!currentDate.isAfter(to)) {
            val indexKey = "timetable:date:$currentDate"
            val dateIds = redisTemplate.opsForSet().members(indexKey)
            dateIds?.forEach { id -> ids.add(id) }
            currentDate = currentDate.plusDays(1)
        }

        return ids
    }

    private fun findEntitiesByIds(ids: Set<String>): List<TimetableRedisEntity> {
        if (ids.isEmpty()) return emptyList()

        return ids.mapNotNull { id ->
            val key = "timetable:$id"
            val hash = redisTemplate.opsForHash<String, String>().entries(key)
            if (hash.isEmpty()) null else convertHashToEntity(hash)
        }
    }

    private fun convertHashToEntity(hash: Map<String, String>): TimetableRedisEntity =
        TimetableRedisEntity(
            id = hash["id"] ?: throw IllegalArgumentException("TimetableRedisEntity ID cannot be null"),
            schoolCode = hash["schoolCode"] ?: throw IllegalArgumentException("TimetableRedisEntity schoolCode cannot be null"),
            schoolName = hash["schoolName"] ?: throw IllegalArgumentException("TimetableRedisEntity schoolName cannot be null"),
            officeCode = hash["officeCode"] ?: throw IllegalArgumentException("TimetableRedisEntity officeCode cannot be null"),
            officeName = hash["officeName"] ?: throw IllegalArgumentException("TimetableRedisEntity officeName cannot be null"),
            date = LocalDate.parse(hash["date"] ?: throw IllegalArgumentException("TimetableRedisEntity date cannot be null")),
            academicYear = hash["academicYear"] ?: throw IllegalArgumentException("TimetableRedisEntity academicYear cannot be null"),
            semester = hash["semester"],
            grade = (hash["grade"] ?: throw IllegalArgumentException("TimetableRedisEntity grade cannot be null")).toInt(),
            classNum = (hash["classNum"] ?: throw IllegalArgumentException("TimetableRedisEntity classNum cannot be null")).toInt(),
            period = (hash["period"] ?: throw IllegalArgumentException("TimetableRedisEntity period cannot be null")).toInt(),
            subject = hash["subject"],
        )
}
