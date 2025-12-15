package team.themoment.datagsm.domain.neis.meal.service

import team.themoment.datagsm.domain.neis.meal.dto.response.MealResDto
import java.time.LocalDate

interface SearchMealService {
    fun execute(
        date: LocalDate?,
        fromDate: LocalDate?,
        toDate: LocalDate?,
    ): List<MealResDto>
}
