package team.themoment.datagsm.common.domain.event.dto.payload

import com.fasterxml.jackson.annotation.JsonProperty

data class EventPayload(
    @field:JsonProperty("id")
    val id: String,
    @field:JsonProperty("event")
    val event: String,
    @field:JsonProperty("timestamp")
    val timestamp: String,
    @field:JsonProperty("data")
    val data: Any,
)
