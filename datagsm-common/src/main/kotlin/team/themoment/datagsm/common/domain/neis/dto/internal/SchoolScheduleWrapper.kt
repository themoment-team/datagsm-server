package team.themoment.datagsm.common.domain.neis.dto.internal

import com.fasterxml.jackson.annotation.JsonProperty

data class SchoolScheduleWrapper(
    @field:JsonProperty("head")
    val head: List<ApiHead>?,
    @field:JsonProperty("row")
    val row: List<SchoolScheduleInfo>?,
)
