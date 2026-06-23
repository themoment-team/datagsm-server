package team.themoment.datagsm.common.domain.event.dto.payload

import com.fasterxml.jackson.annotation.JsonProperty

data class EventStudentRef(
    @field:JsonProperty("student_number")
    val studentNumber: Int?,
    @field:JsonProperty("name")
    val name: String,
)
