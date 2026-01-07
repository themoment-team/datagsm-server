package team.themoment.datagsm.resource.global.thirdparty.feign.neis.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class NeisMealApiResponse(
    @param:JsonProperty("mealServiceDietInfo")
    val mealServiceDietInfo: List<MealServiceDietInfoWrapper>?,
)
