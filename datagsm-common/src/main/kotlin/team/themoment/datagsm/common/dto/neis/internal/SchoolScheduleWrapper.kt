package team.themoment.datagsm.common.dto.neis.internal

import com.fasterxml.jackson.annotation.JsonProperty

data class SchoolScheduleWrapper(
    @param:JsonProperty("head")
    val head: List<ApiHead>?,
    @param:JsonProperty("row")
    val row: List<SchoolScheduleInfo>?,
)
