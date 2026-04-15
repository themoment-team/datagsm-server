package team.themoment.datagsm.web.domain.project.controller

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
import team.themoment.datagsm.common.domain.project.dto.request.EndProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.request.QueryProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectListResDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.web.domain.project.service.CreateProjectService
import team.themoment.datagsm.web.domain.project.service.DeleteProjectService
import team.themoment.datagsm.web.domain.project.service.EndProjectService
import team.themoment.datagsm.web.domain.project.service.ModifyProjectService
import team.themoment.datagsm.web.domain.project.service.QueryProjectService
import team.themoment.datagsm.web.domain.project.service.ReactivateProjectService

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
    @Operation(summary = "프로젝트 정보 조회", description = "필터 조건에 맞는 프로젝트 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
        ],
    )
    @GetMapping
    fun getProjectInfo(
        @Valid @ModelAttribute queryReq: QueryProjectReqDto,
    ): ProjectListResDto = queryProjectService.execute(queryReq)

    @Operation(summary = "프로젝트 생성", description = "새로운 프로젝트를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 프로젝트", content = [Content()]),
        ],
    )
    @PostMapping
    fun createProject(
        @RequestBody @Valid reqDto: ProjectReqDto,
    ): ProjectResDto = createProjectService.execute(reqDto)

    @Operation(summary = "프로젝트 정보 수정", description = "기존 프로젝트의 정보를 전체 교체합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "프로젝트 또는 동아리를 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 프로젝트 이름", content = [Content()]),
        ],
    )
    @PutMapping("/{projectId}")
    fun updateProject(
        @Parameter(description = "프로젝트 ID") @PathVariable projectId: Long,
        @RequestBody @Valid reqDto: ProjectReqDto,
    ): ProjectResDto = modifyProjectService.execute(projectId, reqDto)

    @Operation(summary = "프로젝트 종료", description = "운영 중인 프로젝트를 종료 처리합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "종료 처리 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패 또는 종료 연도 오류)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음", content = [Content()]),
        ],
    )
    @PostMapping("/{projectId}/end")
    fun endProject(
        @Parameter(description = "프로젝트 ID") @PathVariable projectId: Long,
        @RequestBody @Valid reqDto: EndProjectReqDto,
    ) = endProjectService.execute(projectId, reqDto)

    @Operation(summary = "프로젝트 운영 재개", description = "종료된 프로젝트를 다시 운영 중 상태로 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "운영 재개 성공"),
            ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음", content = [Content()]),
        ],
    )
    @PostMapping("/{projectId}/reactivate")
    fun reactivateProject(
        @Parameter(description = "프로젝트 ID") @PathVariable projectId: Long,
    ) = reactivateProjectService.execute(projectId)

    @Operation(summary = "프로젝트 삭제", description = "기존 프로젝트를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "삭제 성공"),
            ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음", content = [Content()]),
        ],
    )
    @DeleteMapping("/{projectId}")
    fun deleteProject(
        @Parameter(description = "프로젝트 ID") @PathVariable projectId: Long,
    ) = deleteProjectService.execute(projectId)
}
