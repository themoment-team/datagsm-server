package team.themoment.datagsm.common.domain.project.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.ksp.annotation.SdkExport

@SdkExport
data class MyProjectListResDto(
    @field:Schema(description = "전체 프로젝트 수", example = "3")
    val totalElements: Int,
    @field:Schema(description = "프로젝트 목록")
    val projects: List<MyProjectResDto>,
)
