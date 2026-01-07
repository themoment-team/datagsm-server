package team.themoment.datagsm.resource.domain.neis.meal.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.common.domain.neis.MealRedisEntity
import java.time.LocalDate

interface MealRedisRepository : CrudRepository<MealRedisEntity, String> {
    fun findByDate(date: LocalDate): List<MealRedisEntity>

    fun findByDateBetween(
        from: LocalDate,
        to: LocalDate,
    ): List<MealRedisEntity>

    fun findByDateGreaterThanEqual(fromDate: LocalDate): List<MealRedisEntity>

    fun findByDateLessThanEqual(toDate: LocalDate): List<MealRedisEntity>
}
