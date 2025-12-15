package team.themoment.datagsm.global.thirdparty.feign.neis.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class NeisMealApiResponse(
    @param:JsonProperty("mealServiceDietInfo")
    val mealServiceDietInfo: List<MealServiceDietInfoWrapper>?,
)
