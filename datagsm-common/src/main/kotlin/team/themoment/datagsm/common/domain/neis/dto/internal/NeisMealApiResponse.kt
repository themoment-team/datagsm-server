package team.themoment.datagsm.common.domain.neis.dto.internal

import com.fasterxml.jackson.annotation.JsonProperty

data class NeisMealApiResponse(
    @param:JsonProperty("mealServiceDietInfo")
    val mealServiceDietInfo: List<MealServiceDietInfoWrapper>?,
)
