package team.themoment.datagsm.resource.domain.neis.meal.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.neis.meal.repository.MealRedisRepository
import team.themoment.datagsm.common.dto.neis.meal.response.MealResDto
import team.themoment.datagsm.resource.domain.neis.meal.service.SearchMealService
import java.time.LocalDate

@Service
class SearchMealServiceImpl(
    private val mealRedisRepository: MealRedisRepository,
) : SearchMealService {
    override fun execute(
        date: LocalDate?,
        fromDate: LocalDate?,
        toDate: LocalDate?,
    ): List<MealResDto> {
        val meals =
            when {
                date != null -> mealRedisRepository.findByDate(date)
                fromDate != null && toDate != null -> mealRedisRepository.findByDateBetween(fromDate, toDate)
                fromDate != null -> mealRedisRepository.findByDateGreaterThanEqual(fromDate)
                toDate != null -> mealRedisRepository.findByDateLessThanEqual(toDate)
                else -> mealRedisRepository.findAll().toList()
            }

        return meals.map { meal ->
            MealResDto(
                mealId = meal.id,
                schoolCode = meal.schoolCode,
                schoolName = meal.schoolName,
                officeCode = meal.officeCode,
                officeName = meal.officeName,
                mealDate = meal.date,
                mealType = meal.type,
                mealMenu = meal.menu,
                mealAllergyInfo = meal.allergyInfo,
                mealCalories = meal.calories,
                originInfo = meal.originInfo,
                nutritionInfo = meal.nutritionInfo,
                mealServeCount = meal.serveCount,
            )
        }
    }
}
