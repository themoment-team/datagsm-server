package team.themoment.datagsm.common.domain.event.dto.payload

import com.fasterxml.jackson.annotation.JsonProperty

// payload 패키지: 컨트롤러 응답이 아닌 webhook(RestClient)으로 외부 구독자에게 전송되는 DTO 모음
// EventPayload: webhook 본문 최상위 봉투
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
