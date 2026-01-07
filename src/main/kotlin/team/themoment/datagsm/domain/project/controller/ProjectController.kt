package team.themoment.datagsm.domain.project.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.account.ApiScope
import team.themoment.datagsm.common.domain.project.ProjectSortBy
import team.themoment.datagsm.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.domain.project.dto.response.ProjectListResDto
import team.themoment.datagsm.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.domain.project.service.CreateProjectService
import team.themoment.datagsm.domain.project.service.DeleteProjectService
import team.themoment.datagsm.domain.project.service.ModifyProjectService
import team.themoment.datagsm.domain.project.service.QueryProjectService
import team.themoment.datagsm.global.common.constant.SortDirection
import team.themoment.datagsm.global.security.annotation.RequireScope

@Tag(name = "Project", description = "프로젝트 관련 API")
@RestController
@RequestMapping("/v1/projects")
class ProjectController(
    private val queryProjectService: QueryProjectService,
    private val createProjectService: CreateProjectService,
    private val modifyProjectService: ModifyProjectService,
    private val deleteProjectService: DeleteProjectService,
) {
    @Operation(summary = "프로젝트 정보 조회", description = "필터 조건에 맞는 프로젝트 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
        ],
    )
    @RequireScope(ApiScope.PROJECT_READ)
    @GetMapping
    fun getProjectInfo(
        @Parameter(description = "프로젝트 ID") @RequestParam(required = false) projectId: Long?,
        @Parameter(description = "프로젝트 이름") @RequestParam(required = false) projectName: String?,
        @Parameter(description = "동아리 ID") @RequestParam(required = false) clubId: Long?,
        @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(required = false, defaultValue = "100") size: Int,
        @Parameter(description = "정렬 기준 (ID, NAME)") @RequestParam(required = false) sortBy: ProjectSortBy?,
        @Parameter(description = "정렬 방향 (ASC, DESC)") @RequestParam(required = false, defaultValue = "ASC") sortDirection: SortDirection,
    ): ProjectListResDto = queryProjectService.execute(projectId, projectName, clubId, page, size, sortBy, sortDirection)

    @Operation(summary = "프로젝트 생성", description = "새로운 프로젝트를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 프로젝트", content = [Content()]),
        ],
    )
    @RequireScope(ApiScope.PROJECT_WRITE)
    @PostMapping
    fun createProject(
        @RequestBody @Valid projectReqDto: ProjectReqDto,
    ): ProjectResDto = createProjectService.execute(projectReqDto)

    @Operation(summary = "프로젝트 정보 수정", description = "기존 프로젝트의 정보를 전체 교체합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "프로젝트 또는 동아리를 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 프로젝트 이름", content = [Content()]),
        ],
    )
    @RequireScope(ApiScope.PROJECT_WRITE)
    @PutMapping("/{projectId}")
    fun updateProject(
        @Parameter(description = "프로젝트 ID") @PathVariable projectId: Long,
        @RequestBody @Valid projectReqDto: ProjectReqDto,
    ): ProjectResDto = modifyProjectService.execute(projectId, projectReqDto)

    @Operation(summary = "프로젝트 삭제", description = "기존 프로젝트를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "삭제 성공"),
            ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음", content = [Content()]),
        ],
    )
    @RequireScope(ApiScope.PROJECT_WRITE)
    @DeleteMapping("/{projectId}")
    fun deleteProject(
        @Parameter(description = "프로젝트 ID") @PathVariable projectId: Long,
    ) = deleteProjectService.execute(projectId)
}
