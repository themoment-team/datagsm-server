package team.themoment.datagsm.common.domain.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectCategory

data class ApplyProjectReqDto(
    @field:NotBlank
    @field:Size(max = 100)
    @param:Schema(
        description = "프로젝트 이름",
        example = "DataGSM 프로젝트",
        maxLength = 100,
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val name: String,
    @field:NotBlank
    @field:Size(max = 500)
    @param:Schema(
        description = "프로젝트 설명",
        example = "학교 데이터를 제공하는 API 서비스",
        maxLength = 500,
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val description: String,
    @field:Positive
    @param:Schema(
        description = "프로젝트 서비스 시작 연도",
        example = "2024",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "1",
    )
    val startYear: Int,
    @param:Schema(
        description = "프로젝트 카테고리 목록 (TOY_PROJECT, TEAM, CLUB, IDEA_FESTIVAL, NOT_DEPLOYED 중 복수 선택 가능). 수정 시 생략하면 기존 값을 유지하고 빈 목록이면 모두 해제한다",
        example = "[\"TEAM\", \"CLUB\"]",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val categories: Set<ProjectCategory>? = null,
    @param:Schema(
        description = "프로젝트 소유 동아리 ID (무소속인 경우 0 또는 미입력)",
        example = "1",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val clubId: Long? = null,
    @param:Schema(
        description = "프로젝트 참여자 학생 ID 목록",
        example = "[1, 2, 3]",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val participantIds: List<Long> = emptyList(),
    @field:Size(max = 20)
    @param:Schema(
        description = "프로젝트 리포지토리 URL 목록",
        example = "[\"https://github.com/team/repo\"]",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val repositories: List<
        @Size(max = 300)
        String,
    > = emptyList(),
    @field:Size(max = 20)
    @param:Schema(
        description = "프로젝트 기술 스택 목록",
        example = "[\"Kotlin\", \"Spring Boot\"]",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val techStacks: List<
        @Size(max = 50)
        String,
    > = emptyList(),
    @field:Size(max = 300)
    @param:Schema(
        description =
            "web의 /v1/students/me/projects/icons/upload-url에서 발급받은 S3 오브젝트 키 (openapi에는 발급 API 없음). 설정된 접두사/36자리 " +
                "키.png·jpg·webp·gif 형식만 허용하며 형식 오류는 400. 수정 시 생략 또는 null이면 유지하고 빈 문자열·공백이면 삭제한다",
        example = "project-icons/3f2504e0-4f89-11d3-9a0c-0305e82c3301.png",
        maxLength = 300,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val iconKey: String? = null,
    @field:Size(max = 300)
    @field:Pattern(regexp = "^$|^https?://.*", message = "URL은 http:// 또는 https://로 시작해야 합니다.")
    @param:Schema(
        description = "프로젝트 배포 URL. 생략 또는 null이면 기존 값을 유지하고 빈 문자열이면 삭제한다",
        example = "https://datagsm.kr",
        maxLength = 300,
        pattern = "^$|^https?://.*",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val deploymentUrl: String? = null,
)
