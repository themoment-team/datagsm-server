package team.themoment.datagsm.common.domain.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus

data class ProjectReqDto(
    @field:NotBlank
    @field:Size(max = 100)
    @param:Schema(description = "프로젝트 이름", example = "DataGSM 프로젝트", maxLength = 100)
    val name: String,
    @field:NotBlank
    @field:Size(max = 500)
    @param:Schema(description = "프로젝트 설명", example = "학교 데이터를 제공하는 API 서비스", maxLength = 500)
    val description: String,
    @field:Positive
    @param:Schema(description = "프로젝트 시작 연도", example = "2024")
    val startYear: Int,
    @param:Schema(description = "프로젝트 소유 동아리 ID", example = "1")
    val clubId: Long?,
    @param:Schema(description = "프로젝트 참가자 학생 ID 목록", example = "[1, 2, 3]")
    val participantIds: List<Long>,
    @param:Schema(description = "프로젝트 운영 상태", example = "ACTIVE")
    val status: ProjectStatus = ProjectStatus.ACTIVE,
    @field:Positive
    @param:Schema(description = "프로젝트 종료 연도 (ENDED 시 설정)", example = "2025")
    val endYear: Int? = null,
)
