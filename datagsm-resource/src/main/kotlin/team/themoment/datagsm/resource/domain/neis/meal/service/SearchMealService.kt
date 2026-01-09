package team.themoment.datagsm.resource.domain.neis.meal.service

import team.themoment.datagsm.common.dto.neis.meal.response.MealResDto
import java.time.LocalDate

interface SearchMealService {
    fun execute(
        date: LocalDate?,
        fromDate: LocalDate?,
        toDate: LocalDate?,
    ): List<MealResDto>
}
