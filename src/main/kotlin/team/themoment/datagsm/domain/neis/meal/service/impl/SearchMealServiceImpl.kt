package team.themoment.datagsm.domain.neis.meal.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.domain.neis.meal.dto.response.MealResDto
import team.themoment.datagsm.domain.neis.meal.repository.MealRedisRepository
import team.themoment.datagsm.domain.neis.meal.service.SearchMealService
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
        val meals = mealRedisRepository.findAll()

        return meals
            .filter { meal ->
                when {
                    date != null -> meal.date == date
                    fromDate != null && toDate != null -> meal.date in fromDate..toDate
                    fromDate != null -> meal.date >= fromDate
                    toDate != null -> meal.date <= toDate
                    else -> true
                }
            }.map { meal ->
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
