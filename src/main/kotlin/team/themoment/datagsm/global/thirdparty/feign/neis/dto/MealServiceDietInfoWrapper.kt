package team.themoment.datagsm.global.thirdparty.feign.neis.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class MealServiceDietInfoWrapper(
    @param:JsonProperty("head")
    val head: List<ApiHead>?,
    @param:JsonProperty("row")
    val row: List<MealServiceDietInfo>?,
)
