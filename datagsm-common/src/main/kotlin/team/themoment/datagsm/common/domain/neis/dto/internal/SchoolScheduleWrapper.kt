package team.themoment.datagsm.common.domain.neis.dto.internal

import com.fasterxml.jackson.annotation.JsonProperty

data class SchoolScheduleWrapper(
    @param:JsonProperty("head")
    val head: List<ApiHead>?,
    @param:JsonProperty("row")
    val row: List<SchoolScheduleInfo>?,
)
