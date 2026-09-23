package team.themoment.datagsm.common.domain.project.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus
import team.themoment.datagsm.common.domain.student.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.ksp.annotation.SdkExport
import java.time.LocalDateTime

@SdkExport
data class ProjectEditRequestResDto(
    @field:Schema(description = "신청 ID", example = "1")
    val id: Long,
    @field:Schema(description = "수정 대상 프로젝트 ID (신규 생성 신청이면 null)", example = "10")
    val originalProjectId: Long?,
    @field:Schema(description = "신청자 정보")
    val requestedBy: ParticipantInfoDto,
    @field:Schema(description = "프로젝트 이름", example = "DataGSM 프로젝트")
    val name: String,
    @field:Schema(description = "프로젝트 설명", example = "학교 데이터를 제공하는 API 서비스")
    val description: String,
    @field:Schema(description = "프로젝트 서비스 시작 연도", example = "2024")
    val startYear: Int,
    @field:Schema(description = "프로젝트 아이콘 URL", example = "https://cdn.datagsm.kr/project-icons/uuid.png")
    val iconUrl: String?,
    @field:Schema(
        description = "프로젝트 아이콘 오브젝트 키. 수정 신청 시 아이콘을 바꾸지 않으려면 이 값을 그대로 보내거나 생략한다",
        example = "project-icons/3f2504e0-4f89-11d3-9a0c-0305e82c3301.png",
    )
    val iconKey: String?,
    @field:Schema(description = "프로젝트 배포 URL", example = "https://datagsm.kr")
    val deploymentUrl: String?,
    @field:Schema(description = "프로젝트 소유 동아리 정보")
    val club: ClubSummaryDto?,
    @field:Schema(description = "프로젝트 참여자 목록")
    val participants: List<ParticipantInfoDto>,
    @field:Schema(description = "프로젝트 리포지토리 URL 목록")
    val repositories: List<String>,
    @field:Schema(description = "프로젝트 기술 스택 목록")
    val techStacks: List<String>,
    @field:Schema(description = "신청 상태")
    val requestStatus: ProjectRequestStatus,
    @field:Schema(description = "거절 사유 (거절된 경우에만 존재)")
    val rejectReason: String?,
    @field:Schema(description = "신청 일시")
    val requestedAt: LocalDateTime,
    @field:Schema(description = "처리 일시 (수락/거절된 경우에만 존재)")
    val processedAt: LocalDateTime?,
)
