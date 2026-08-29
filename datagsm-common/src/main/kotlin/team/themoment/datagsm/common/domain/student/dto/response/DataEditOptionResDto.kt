package team.themoment.datagsm.common.domain.student.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class DataEditOptionResDto(
    @field:Schema(description = "선택지 값 (동아리 ID)", example = "3")
    val value: Long,
    @field:Schema(description = "선택지 라벨 (동아리 이름)", example = "미림")
    val label: String,
)
