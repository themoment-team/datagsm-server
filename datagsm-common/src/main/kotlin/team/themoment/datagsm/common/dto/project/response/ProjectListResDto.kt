package team.themoment.datagsm.common.dto.project.response

import io.swagger.v3.oas.annotations.media.Schema

data class ProjectListResDto(
    @param:Schema(description = "전체 페이지 수", example = "1")
    val totalPages: Int,
    @param:Schema(description = "전체 프로젝트 수", example = "20")
    val totalElements: Long,
    @param:Schema(description = "프로젝트 목록")
    val projects: List<ProjectResDto>,
)
