package team.themoment.datagsm.common.domain.student.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.student.entity.constant.StudentDataEditField

data class DataEditFieldResDto(
    @field:Schema(description = "수정이 필요한 필드", example = "MAJOR_CLUB")
    val name: StudentDataEditField,
    @field:Schema(description = "선택지 목록 (동아리 필드일 때만 존재)")
    val options: List<DataEditOptionResDto>? = null,
)
