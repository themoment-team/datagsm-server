package team.themoment.datagsm.common.domain.event.dto.payload

import com.fasterxml.jackson.annotation.JsonProperty

// webhook 전송용 payload: project.changed 의 object 에 담기는 프로젝트 전체 정보
data class ProjectEventObject(
    @field:JsonProperty("project_id")
    val projectId: Long,
    @field:JsonProperty("name")
    val name: String,
    @field:JsonProperty("description")
    val description: String,
    @field:JsonProperty("start_year")
    val startYear: Int,
    @field:JsonProperty("end_year")
    val endYear: Int?,
    @field:JsonProperty("status")
    val status: String,
    @field:JsonProperty("club")
    val club: EventClubRef?,
    @field:JsonProperty("participants")
    val participants: List<EventStudentRef>,
)
