package team.themoment.datagsm.openapi.domain.project.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.auth.entity.constant.ApiKeyScope
import team.themoment.datagsm.common.domain.project.dto.request.EndProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.request.QueryProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectListResDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.openapi.domain.project.service.CreateProjectService
import team.themoment.datagsm.openapi.domain.project.service.DeleteProjectService
import team.themoment.datagsm.openapi.domain.project.service.EndProjectService
import team.themoment.datagsm.openapi.domain.project.service.ModifyProjectService
import team.themoment.datagsm.openapi.domain.project.service.QueryProjectService
import team.themoment.datagsm.openapi.domain.project.service.ReactivateProjectService
import team.themoment.datagsm.openapi.global.security.annotation.RequireScope

@Tag(name = "Project", description = "프로젝트 관련 API")
@RestController
@RequestMapping("/v1/projects")
class ProjectController(
    private val queryProjectService: QueryProjectService,
    private val createProjectService: CreateProjectService,
    private val modifyProjectService: ModifyProjectService,
    private val deleteProjectService: DeleteProjectService,
    private val endProjectService: EndProjectService,
    private val reactivateProjectService: ReactivateProjectService,
) {
    @Operation(
        summary = "프로젝트 정보 조회",
        description = "필터 조건에 맞는 프로젝트 정보를 조회합니다. (필요 scope: project:read)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "API 키가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "403", description = "필요한 API 키 scope가 없음", content = [Content()]),
            ApiResponse(responseCode = "429", description = "API 키 요청 제한 초과", content = [Content()]),
        ],
    )
    @RequireScope(ApiKeyScope.PROJECT_READ)
    @GetMapping
    fun getProjectInfo(
        @Valid @ModelAttribute queryReq: QueryProjectReqDto,
    ): ProjectListResDto = queryProjectService.execute(queryReq)

    @Operation(
        summary = "프로젝트 생성",
        description = "새로운 프로젝트를 생성합니다. ENDED 상태이면 시작 연도 이상인 endYear가 필수이며, ACTIVE 상태에서도 전달한 endYear를 저장합니다. (필요 scope: project:write)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "생성 성공"),
            ApiResponse(responseCode = "400", description = "검증 실패, ENDED 상태의 종료 연도 누락·시작 연도 미만 또는 잘못된 아이콘 키", content = [Content()]),
            ApiResponse(responseCode = "401", description = "API 키가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "403", description = "필요한 API 키 scope가 없음", content = [Content()]),
            ApiResponse(responseCode = "404", description = "동아리 또는 참여자 학생을 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 프로젝트", content = [Content()]),
            ApiResponse(responseCode = "429", description = "API 키 요청 제한 초과", content = [Content()]),
        ],
    )
    @RequireScope(ApiKeyScope.PROJECT_WRITE)
    @PostMapping
    fun createProject(
        @RequestBody @Valid reqDto: ProjectReqDto,
    ): ProjectResDto = createProjectService.execute(reqDto)

    @Operation(
        summary = "프로젝트 정보 수정",
        description =
            "프로젝트 정보를 수정합니다. status와 endYear는 반영하지 않으며 종료·재개 API로만 변경합니다. iconKey, deploymentUrl, categories는 " +
                "생략하거나 null이면 기존 값을 유지합니다. repositories와 techStacks는 생략하면 빈 목록으로 교체됩니다. (필요 scope: project:write)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(
                responseCode = "400",
                description = "검증 실패 또는 잘못된 아이콘 키 (status와 endYear는 반영하지 않지만 요청 검증은 적용)",
                content = [Content()],
            ),
            ApiResponse(responseCode = "401", description = "API 키가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "403", description = "필요한 API 키 scope가 없음", content = [Content()]),
            ApiResponse(responseCode = "404", description = "프로젝트, 동아리 또는 참여자 학생을 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 프로젝트 이름", content = [Content()]),
            ApiResponse(responseCode = "429", description = "API 키 요청 제한 초과", content = [Content()]),
        ],
    )
    @RequireScope(ApiKeyScope.PROJECT_WRITE)
    @PutMapping("/{projectId}")
    fun updateProject(
        @Parameter(description = "프로젝트 ID") @PathVariable projectId: Long,
        @RequestBody @Valid reqDto: ProjectReqDto,
    ): ProjectResDto = modifyProjectService.execute(projectId, reqDto)

    @Operation(
        summary = "프로젝트 종료",
        description = "현재 상태와 관계없이 ENDED로 변경하고 종료 연도를 설정합니다. 이미 종료된 프로젝트도 종료 연도를 덮어씁니다. (필요 scope: project:write)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "종료 처리 성공", content = [Content()]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패 또는 종료 연도 오류)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "API 키가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "403", description = "필요한 API 키 scope가 없음", content = [Content()]),
            ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "429", description = "API 키 요청 제한 초과", content = [Content()]),
        ],
    )
    @RequireScope(ApiKeyScope.PROJECT_WRITE)
    @PostMapping("/{projectId}/end")
    fun endProject(
        @Parameter(description = "프로젝트 ID") @PathVariable projectId: Long,
        @RequestBody @Valid reqDto: EndProjectReqDto,
    ) = endProjectService.execute(projectId, reqDto)

    @Operation(
        summary = "프로젝트 운영 재개",
        description = "현재 상태와 관계없이 ACTIVE로 변경하고 종료 연도를 null로 초기화합니다. (필요 scope: project:write)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "운영 재개 성공", content = [Content()]),
            ApiResponse(responseCode = "401", description = "API 키가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "403", description = "필요한 API 키 scope가 없음", content = [Content()]),
            ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "429", description = "API 키 요청 제한 초과", content = [Content()]),
        ],
    )
    @RequireScope(ApiKeyScope.PROJECT_WRITE)
    @PostMapping("/{projectId}/reactivate")
    fun reactivateProject(
        @Parameter(description = "프로젝트 ID") @PathVariable projectId: Long,
    ) = reactivateProjectService.execute(projectId)

    @Operation(
        summary = "프로젝트 삭제",
        description = "프로젝트와 해당 프로젝트를 참조하는 신청 이력을 함께 삭제합니다. (필요 scope: project:write)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 성공", content = [Content()]),
            ApiResponse(responseCode = "401", description = "API 키가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "403", description = "필요한 API 키 scope가 없음", content = [Content()]),
            ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "429", description = "API 키 요청 제한 초과", content = [Content()]),
        ],
    )
    @RequireScope(ApiKeyScope.PROJECT_WRITE)
    @DeleteMapping("/{projectId}")
    fun deleteProject(
        @Parameter(description = "프로젝트 ID") @PathVariable projectId: Long,
    ) = deleteProjectService.execute(projectId)
}
