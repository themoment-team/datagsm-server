package team.themoment.datagsm.common.domain.event.dto.payload

import com.fasterxml.jackson.annotation.JsonProperty

// webhook 전송용 payload: 프로젝트 object 내 소속 동아리를 가리키는 참조
data class EventClubRef(
    @field:JsonProperty("club_id")
    val clubId: Long,
    @field:JsonProperty("name")
    val name: String,
)
