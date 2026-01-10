package team.themoment.datagsm.resource.domain.project.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.account.entity.constant.ApiScope
import team.themoment.datagsm.common.domain.project.dto.response.ProjectListResDto
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectSortBy
import team.themoment.datagsm.common.global.constant.SortDirection
import team.themoment.datagsm.resource.domain.project.service.QueryProjectService
import team.themoment.datagsm.resource.global.security.annotation.RequireScope

@Tag(name = "Project", description = "프로젝트 관련 API")
@RestController
@RequestMapping("/v1/projects")
class ProjectController(
    private val queryProjectService: QueryProjectService,
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
}
