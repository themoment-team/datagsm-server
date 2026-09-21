package team.themoment.datagsm.common.domain.project.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.ksp.annotation.SdkExport

@SdkExport
data class ProjectEditRequestListResDto(
    @field:Schema(description = "전체 페이지 수", example = "1")
    val totalPages: Int,
    @field:Schema(description = "전체 신청 수", example = "20")
    val totalElements: Long,
    @field:Schema(description = "프로젝트 신청 목록")
    val requests: List<ProjectEditRequestResDto>,
)
