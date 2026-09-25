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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.project.dto.request.QueryPublicProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.PublicProjectListResDto
import team.themoment.datagsm.common.domain.project.dto.response.PublicProjectResDto
import team.themoment.datagsm.web.domain.project.service.QueryPublicProjectService

@Tag(name = "Public Project", description = "인증 없이 조회 가능한 프로젝트 공개 API")
@RestController
@RequestMapping("/v1/public/projects")
class PublicProjectController(
    private val queryPublicProjectService: QueryPublicProjectService,
) {
    @Operation(
        summary = "공개 프로젝트 목록 조회",
        description = "등록된 프로젝트 목록을 인증 없이 조회합니다. IP 기준 요청 제한이 적용됩니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "429", description = "요청 제한 초과", content = [Content()]),
        ],
    )
    @GetMapping
    fun getPublicProjects(
        @Valid @ModelAttribute queryReq: QueryPublicProjectReqDto,
    ): PublicProjectListResDto = queryPublicProjectService.execute(queryReq)

    @Operation(
        summary = "공개 프로젝트 상세 조회",
        description = "프로젝트 단건 정보를 인증 없이 조회합니다. IP 기준 요청 제한이 적용됩니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "429", description = "요청 제한 초과", content = [Content()]),
        ],
    )
    @GetMapping("/{projectId}")
    fun getPublicProject(
        @Parameter(description = "프로젝트 ID") @PathVariable projectId: Long,
    ): PublicProjectResDto = queryPublicProjectService.executeById(projectId)
}
