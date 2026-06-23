package team.themoment.datagsm.common.domain.event.dto.payload

import com.fasterxml.jackson.annotation.JsonProperty

// webhook 전송용 payload: 동아리·프로젝트 object 내 leader·participants 를 가리키는 학생 참조
data class EventStudentRef(
    @field:JsonProperty("student_number")
    val studentNumber: Int?,
    @field:JsonProperty("name")
    val name: String,
)
