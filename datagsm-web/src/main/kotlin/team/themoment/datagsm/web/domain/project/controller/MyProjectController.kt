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

@Tag(name = "My Project", description = "학생 본인의 프로젝트 신청 및 로그인 계정의 아이콘 업로드 URL 발급 API")
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
            ApiResponse(responseCode = "400", description = "잘못된 신청 상태 필터", content = [Content()]),
            ApiResponse(responseCode = "401", description = "로그인 인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "403", description = "학생 정보가 연결되지 않은 계정", content = [Content()]),
            ApiResponse(responseCode = "404", description = "계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @GetMapping
    fun getMyProjects(
        @Valid @ModelAttribute queryReq: QueryMyProjectReqDto,
    ): MyProjectListResDto = queryMyProjectService.execute(queryReq)

    @Operation(
        summary = "프로젝트 신청",
        description = "새로운 프로젝트 등록을 신청합니다. 어드민 수락 후 공식 등록됩니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "신청 성공"),
            ApiResponse(responseCode = "400", description = "검증 실패 또는 잘못된 아이콘 키", content = [Content()]),
            ApiResponse(responseCode = "401", description = "로그인 인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "403", description = "학생 정보가 연결되지 않은 계정", content = [Content()]),
            ApiResponse(responseCode = "404", description = "계정, 동아리 또는 참여자를 찾을 수 없음", content = [Content()]),
        ],
    )
    @PostMapping
    fun applyProject(
        @RequestBody @Valid reqDto: ApplyProjectReqDto,
    ): ProjectEditRequestResDto = applyProjectService.execute(reqDto)

    @Operation(
        summary = "프로젝트 수정 신청",
        description = "기존 프로젝트의 수정을 신청합니다. 신청자 또는 참여자가 수정할 수 있으며, 기존 수정 신청은 처리 상태와 관계없이 최신 내용으로 대체되어 대기 상태가 됩니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 신청 성공"),
            ApiResponse(responseCode = "400", description = "검증 실패 또는 잘못된 아이콘 키", content = [Content()]),
            ApiResponse(responseCode = "401", description = "로그인 인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "403", description = "학생 정보가 연결되지 않았거나 해당 프로젝트의 신청자·참여자가 아님", content = [Content()]),
            ApiResponse(responseCode = "404", description = "프로젝트, 계정, 동아리 또는 참여자를 찾을 수 없음", content = [Content()]),
        ],
    )
    @PutMapping("/{projectId}")
    fun applyProjectModification(
        @Parameter(description = "프로젝트 ID") @PathVariable projectId: Long,
        @RequestBody @Valid reqDto: ApplyProjectReqDto,
    ): ProjectEditRequestResDto = applyProjectModificationService.execute(projectId, reqDto)

    @Operation(
        summary = "프로젝트 아이콘 업로드 URL 발급",
        description = "학생 정보 연결 여부와 관계없이 로그인한 계정에 S3 직접 업로드용 presigned URL을 발급합니다. 계정별 요청 제한이 적용됩니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "발급 성공"),
            ApiResponse(responseCode = "400", description = "검증 실패, 지원하지 않는 이미지 형식 또는 허용 크기 초과", content = [Content()]),
            ApiResponse(responseCode = "401", description = "로그인 인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "429", description = "계정별 아이콘 업로드 요청 제한 초과", content = [Content()]),
        ],
    )
    @PostMapping("/icons/upload-url")
    fun createIconUploadUrl(
        @RequestBody @Valid reqDto: CreateProjectIconUploadUrlReqDto,
    ): ProjectIconUploadUrlResDto = createProjectIconUploadUrlService.execute(reqDto)
}
