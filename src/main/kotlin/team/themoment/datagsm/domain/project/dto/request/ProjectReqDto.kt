package team.themoment.datagsm.domain.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ProjectReqDto(
    @field:NotBlank
    @field:Size(max = 100)
    @param:Schema(description = "프로젝트 이름", example = "DataGSM 프로젝트", maxLength = 100)
    val name: String,
    @field:NotBlank
    @field:Size(max = 500)
    @param:Schema(description = "프로젝트 설명", example = "학교 데이터를 제공하는 API 서비스", maxLength = 500)
    val description: String,
    @param:Schema(description = "프로젝트 소유 동아리 ID", example = "1")
    val clubId: Long,
)
