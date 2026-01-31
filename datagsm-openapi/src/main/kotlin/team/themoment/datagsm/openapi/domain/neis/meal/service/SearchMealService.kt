package team.themoment.datagsm.openapi.domain.neis.meal.service

import team.themoment.datagsm.common.domain.neis.dto.meal.response.MealResDto
import java.time.LocalDate

interface SearchMealService {
    fun execute(
        date: LocalDate?,
        fromDate: LocalDate?,
        toDate: LocalDate?,
    ): List<MealResDto>
}
