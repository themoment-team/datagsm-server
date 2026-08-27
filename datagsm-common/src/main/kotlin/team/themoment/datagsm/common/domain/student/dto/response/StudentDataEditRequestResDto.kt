package team.themoment.datagsm.common.domain.student.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.student.entity.constant.StudentDataEditField

data class StudentDataEditRequestResDto(
    @field:Schema(description = "수정이 필요한 필드 목록", example = "[\"STUDENT_NUMBER\", \"DORMITORY_ROOM_NUMBER\"]")
    val fields: Set<StudentDataEditField>,
)
