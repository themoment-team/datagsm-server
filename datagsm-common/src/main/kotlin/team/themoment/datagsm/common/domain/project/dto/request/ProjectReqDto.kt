package team.themoment.datagsm.common.domain.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
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
    @field:Size(max = 20)
    @param:Schema(description = "프로젝트 리포지토리 URL 목록", example = "[\"https://github.com/team/repo\"]")
    val repositories: List<
        @Size(max = 300)
        String,
    > = emptyList(),
    @field:Size(max = 20)
    @param:Schema(description = "프로젝트 기술 스택 목록", example = "[\"Kotlin\", \"Spring Boot\"]")
    val techStacks: List<
        @Size(max = 50)
        String,
    > = emptyList(),
    @field:Size(max = 300)
    @param:Schema(
        description = "아이콘 업로드 후 발급받은 S3 오브젝트 키. 생략하면 기존 값을 유지하고 빈 문자열이면 삭제한다",
        example = "project-icons/3f2504e0-4f89-11d3-9a0c-0305e82c3301.png",
    )
    val iconKey: String? = null,
    @field:Size(max = 300)
    @field:Pattern(regexp = "^$|^https?://.*", message = "URL은 http:// 또는 https://로 시작해야 합니다.")
    @param:Schema(
        description = "프로젝트 배포 URL. 생략하면 기존 값을 유지하고 빈 문자열이면 삭제한다",
        example = "https://datagsm.kr",
        maxLength = 300,
    )
    val deploymentUrl: String? = null,
)
