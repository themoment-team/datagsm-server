package team.themoment.datagsm.common.domain.event.dto.payload

import com.fasterxml.jackson.annotation.JsonProperty

data class StudentChangedData(
    @field:JsonProperty("old")
    val old: List<StudentEventSnapshot>,
    @field:JsonProperty("new")
    val new: List<StudentEventSnapshot>,
)
