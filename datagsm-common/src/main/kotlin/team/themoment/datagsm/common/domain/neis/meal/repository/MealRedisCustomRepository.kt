package team.themoment.datagsm.common.domain.neis.meal.repository

import team.themoment.datagsm.common.domain.neis.meal.entity.MealRedisEntity
import java.time.LocalDate

interface MealRedisCustomRepository {
    fun findByDateBetween(
        from: LocalDate,
        to: LocalDate,
    ): List<MealRedisEntity>

    fun findByDateGreaterThanEqual(fromDate: LocalDate): List<MealRedisEntity>

    fun findByDateLessThanEqual(toDate: LocalDate): List<MealRedisEntity>
}
