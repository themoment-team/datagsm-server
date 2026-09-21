package team.themoment.datagsm.web.domain.project.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.project.dto.request.ApplyProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.request.CreateProjectIconUploadUrlReqDto
import team.themoment.datagsm.common.domain.project.dto.request.QueryMyProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.MyProjectListResDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectEditRequestResDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectIconUploadUrlResDto
import team.themoment.datagsm.web.domain.project.service.ApplyProjectModificationService
import team.themoment.datagsm.web.domain.project.service.ApplyProjectService
import team.themoment.datagsm.web.domain.project.service.CreateProjectIconUploadUrlService
import team.themoment.datagsm.web.domain.project.service.QueryMyProjectService

@Tag(name = "My Project", description = "학생 본인의 프로젝트 신청 관련 API")
@RestController
@RequestMapping("/v1/students/me/projects")
class MyProjectController(
    private val queryMyProjectService: QueryMyProjectService,
    private val applyProjectService: ApplyProjectService,
    private val applyProjectModificationService: ApplyProjectModificationService,
    private val createProjectIconUploadUrlService: CreateProjectIconUploadUrlService,
) {
    @Operation(
        summary = "내 프로젝트 목록 조회",
        description = "본인이 신청했거나 참여자로 등록된 프로젝트를 신청 상태와 함께 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "403", description = "학생 정보가 연결되지 않은 계정", content = [Content()]),
        ],
    )
    @GetMapping
    fun getMyProjects(
        @Valid @ModelAttribute queryReq: QueryMyProjectReqDto,
    ): MyProjectListResDto = queryMyProjectService.execute(queryReq)

    @Operation(summary = "프로젝트 신청", description = "새로운 프로젝트 등록을 신청합니다. 어드민 수락 후 공식 등록됩니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "신청 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "동아리 또는 참여자를 찾을 수 없음", content = [Content()]),
        ],
    )
    @PostMapping
    fun applyProject(
        @RequestBody @Valid reqDto: ApplyProjectReqDto,
    ): ProjectEditRequestResDto = applyProjectService.execute(reqDto)

    @Operation(
        summary = "프로젝트 수정 신청",
        description = "기존 프로젝트의 수정을 신청합니다. 대기 중인 수정 신청이 있으면 최신 내용으로 대체됩니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 신청 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "403", description = "해당 프로젝트를 수정할 권한 없음", content = [Content()]),
            ApiResponse(responseCode = "404", description = "프로젝트, 동아리 또는 참여자를 찾을 수 없음", content = [Content()]),
        ],
    )
    @PutMapping("/{projectId}")
    fun applyProjectModification(
        @Parameter(description = "프로젝트 ID") @PathVariable projectId: Long,
        @RequestBody @Valid reqDto: ApplyProjectReqDto,
    ): ProjectEditRequestResDto = applyProjectModificationService.execute(projectId, reqDto)

    @Operation(
        summary = "프로젝트 아이콘 업로드 URL 발급",
        description = "아이콘 이미지를 S3에 직접 업로드할 수 있는 presigned URL을 발급합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "발급 성공"),
            ApiResponse(responseCode = "400", description = "지원하지 않는 형식이거나 허용 크기 초과", content = [Content()]),
        ],
    )
    @PostMapping("/icons/upload-url")
    fun createIconUploadUrl(
        @RequestBody @Valid reqDto: CreateProjectIconUploadUrlReqDto,
    ): ProjectIconUploadUrlResDto = createProjectIconUploadUrlService.execute(reqDto)
}
