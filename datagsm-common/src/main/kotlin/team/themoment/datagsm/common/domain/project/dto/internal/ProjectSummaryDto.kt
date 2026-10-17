package team.themoment.datagsm.common.domain.project.dto.internal

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.ksp.annotation.SdkExport

@SdkExport
data class ProjectSummaryDto(
    @field:Schema(description = "프로젝트 ID", example = "1")
    val id: Long,
    @field:Schema(description = "프로젝트 이름", example = "DataGSM")
    val name: String,
    @field:Schema(description = "프로젝트 상태", example = "ACTIVE")
    val status: ProjectStatus,
    @field:Schema(description = "소속 동아리")
    val club: ClubSummaryDto?,
)
