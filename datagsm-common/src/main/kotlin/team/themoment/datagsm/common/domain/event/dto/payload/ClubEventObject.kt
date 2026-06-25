package team.themoment.datagsm.common.domain.event.dto.payload

import com.fasterxml.jackson.annotation.JsonProperty

// webhook 전송용 payload: club.updated 의 object 에 담기는 동아리 전체 정보
data class ClubEventObject(
    @field:JsonProperty("club_id")
    val clubId: Long,
    @field:JsonProperty("name")
    val name: String,
    @field:JsonProperty("type")
    val type: String,
    @field:JsonProperty("founded_year")
    val foundedYear: Int,
    @field:JsonProperty("status")
    val status: String,
    @field:JsonProperty("abolished_year")
    val abolishedYear: Int?,
    @field:JsonProperty("leader")
    val leader: EventStudentRef?,
    @field:JsonProperty("participants")
    val participants: List<EventStudentRef>,
)
