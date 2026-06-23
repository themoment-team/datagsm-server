package team.themoment.datagsm.common.domain.event.dto.payload

import com.fasterxml.jackson.annotation.JsonProperty

data class EventChangeItem(
    @field:JsonProperty("index")
    val index: Int,
    @field:JsonProperty("object")
    val obj: Any,
)
