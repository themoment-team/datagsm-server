package team.themoment.datagsm.common.domain.event.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class EventListResDto(
    @field:Schema(description = "Event 목록")
    @field:JsonProperty("events")
    val events: List<EventResDto>,
)
