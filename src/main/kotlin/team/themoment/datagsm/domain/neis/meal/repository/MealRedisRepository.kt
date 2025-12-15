package team.themoment.datagsm.domain.neis.meal.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.domain.neis.meal.entity.MealRedisEntity

interface MealRedisRepository : CrudRepository<MealRedisEntity, String>
