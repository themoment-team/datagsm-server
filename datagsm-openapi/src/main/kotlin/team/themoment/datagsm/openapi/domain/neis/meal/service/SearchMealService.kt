package team.themoment.datagsm.openapi.domain.neis.meal.service

import team.themoment.datagsm.common.domain.neis.dto.meal.request.QueryMealReqDto
import team.themoment.datagsm.common.domain.neis.dto.meal.response.MealResDto

interface SearchMealService {
    fun execute(reqDto: QueryMealReqDto): MealResDto
}
