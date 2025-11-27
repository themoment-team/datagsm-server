package team.themoment.datagsm.domain.project.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.domain.club.dto.response.ClubResDto

data class ProjectResDto(
    @param:Schema(description = "프로젝트 ID", example = "1")
    val projectId: Long,
    @param:Schema(description = "프로젝트 이름", example = "DataGSM 프로젝트")
    val projectName: String,
    @param:Schema(description = "프로젝트 설명", example = "학교 데이터를 제공하는 API 서비스")
    val projectDescription: String,
    @param:Schema(description = "프로젝트 소유 동아리 정보")
    val projectOwnerClub: ClubResDto,
)
