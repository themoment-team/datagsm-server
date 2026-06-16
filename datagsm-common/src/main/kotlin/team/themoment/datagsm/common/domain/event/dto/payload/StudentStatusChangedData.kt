package team.themoment.datagsm.common.domain.event.dto.payload

import com.fasterxml.jackson.annotation.JsonProperty

data class StudentStatusChangedData(
    @field:JsonProperty("student_id")
    val studentId: Long,
    @field:JsonProperty("name")
    val name: String,
    @field:JsonProperty("email")
    val email: String,
    @field:JsonProperty("status")
    val status: String,
)
