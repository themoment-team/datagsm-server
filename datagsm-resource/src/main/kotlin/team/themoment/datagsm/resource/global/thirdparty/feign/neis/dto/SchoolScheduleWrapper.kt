package team.themoment.datagsm.resource.global.thirdparty.feign.neis.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class SchoolScheduleWrapper(
    @param:JsonProperty("head")
    val head: List<ApiHead>?,
    @param:JsonProperty("row")
    val row: List<SchoolScheduleInfo>?,
)
