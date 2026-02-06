package team.themoment.datagsm.openapi.domain.neis.meal.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.neis.dto.meal.response.MealResDto
import team.themoment.datagsm.common.domain.neis.meal.repository.MealRedisRepository
import team.themoment.datagsm.openapi.domain.neis.meal.service.SearchMealService
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
        val allMeals =
            if (date != null) {
                mealRedisRepository.findByDate(date)
            } else {
                mealRedisRepository.findAll().toList()
            }

        val filteredMeals =
            allMeals.filter { meal ->
                val matchesFromDate = fromDate == null || !meal.date.isBefore(fromDate)
                val matchesToDate = toDate == null || !meal.date.isAfter(toDate)
                matchesFromDate && matchesToDate
            }

        return filteredMeals.map { meal ->
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
