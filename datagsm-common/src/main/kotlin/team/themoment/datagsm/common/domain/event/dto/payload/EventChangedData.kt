package team.themoment.datagsm.common.domain.event.dto.payload

import com.fasterxml.jackson.annotation.JsonProperty

// webhook 전송용 payload: changed 이벤트의 변경 전(old)/후(new) 리스트
data class EventChangedData(
    @field:JsonProperty("old")
    val old: List<EventChangeItem>,
    @field:JsonProperty("new")
    val new: List<EventChangeItem>,
)
