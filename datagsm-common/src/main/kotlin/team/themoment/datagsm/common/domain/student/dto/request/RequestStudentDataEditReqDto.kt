package team.themoment.datagsm.common.domain.student.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import team.themoment.datagsm.common.domain.student.entity.constant.StudentDataEditField

data class RequestStudentDataEditReqDto(
    @field:NotEmpty(message = "대상 학생 ID는 최소 1개 이상이어야 합니다.")
    @param:Schema(description = "정보 수정을 요청할 학생 ID 목록", example = "[1, 2, 3]")
    val studentIds: List<Long>,
    @field:NotEmpty(message = "수정 요청 항목은 최소 1개 이상이어야 합니다.")
    @param:Schema(description = "수정을 요청할 필드 목록", example = "[\"STUDENT_NUMBER\", \"DORMITORY_ROOM_NUMBER\"]")
    val fields: Set<StudentDataEditField>,
)
