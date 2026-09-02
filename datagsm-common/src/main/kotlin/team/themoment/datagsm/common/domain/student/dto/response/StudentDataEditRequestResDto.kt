package team.themoment.datagsm.common.domain.student.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class StudentDataEditRequestResDto(
    @field:Schema(description = "수정이 필요한 필드와 선택지 목록")
    val fields: List<DataEditFieldResDto>,
)
