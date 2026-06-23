package team.themoment.datagsm.common.domain.event.dto.payload

import com.fasterxml.jackson.annotation.JsonProperty

data class EventChangedData(
    @field:JsonProperty("old")
    val old: List<EventChangeItem>,
    @field:JsonProperty("new")
    val new: List<EventChangeItem>,
)
