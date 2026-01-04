package team.themoment.datagsm.domain.project.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.domain.student.dto.internal.ParticipantInfoDto

data class ProjectResDto(
    @param:Schema(description = "프로젝트 ID", example = "1")
    val id: Long,
    @param:Schema(description = "프로젝트 이름", example = "DataGSM 프로젝트")
    val name: String,
    @param:Schema(description = "프로젝트 설명", example = "학교 데이터를 제공하는 API 서비스")
    val description: String,
    @param:Schema(description = "프로젝트 소유 동아리 정보")
    val club: ClubSummaryDto?,
    @param:Schema(description = "프로젝트 참가자 목록")
    val participants: List<ParticipantInfoDto>,
)
