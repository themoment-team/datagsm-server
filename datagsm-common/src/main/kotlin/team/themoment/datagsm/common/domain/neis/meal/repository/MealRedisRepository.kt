package team.themoment.datagsm.common.domain.neis.meal.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.common.domain.neis.meal.entity.MealRedisEntity
import java.time.LocalDate

interface MealRedisRepository :
    CrudRepository<MealRedisEntity, String>,
    MealRedisCustomRepository {
    fun findByDate(date: LocalDate): List<MealRedisEntity>
}
