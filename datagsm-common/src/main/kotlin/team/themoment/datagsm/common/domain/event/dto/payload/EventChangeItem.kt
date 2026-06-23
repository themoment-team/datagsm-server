package team.themoment.datagsm.common.domain.event.dto.payload

import com.fasterxml.jackson.annotation.JsonProperty

// webhook 전송용 payload: old/new 리스트의 한 항목. object가 비면 생성(old) 또는 삭제(new)
data class EventChangeItem(
    @field:JsonProperty("index")
    val index: Int,
    @field:JsonProperty("object")
    val obj: Any,
)
