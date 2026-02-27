package team.themoment.datagsm.common.domain.project.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class ProjectListResDto(
    @field:Schema(description = "전체 페이지 수", example = "1")
    val totalPages: Int,
    @field:Schema(description = "전체 프로젝트 수", example = "20")
    val totalElements: Long,
    @field:Schema(description = "프로젝트 목록")
    val projects: List<ProjectResDto>,
)
