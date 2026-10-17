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
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.project.dto.request.QueryProjectEditRequestReqDto
import team.themoment.datagsm.common.domain.project.dto.request.RejectProjectRequestReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectEditRequestListResDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectEditRequestResDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.web.domain.project.service.AcceptProjectRequestService
import team.themoment.datagsm.web.domain.project.service.QueryProjectEditRequestService
import team.themoment.datagsm.web.domain.project.service.RejectProjectRequestService

@Tag(name = "Project Request", description = "프로젝트 신청 심사 관련 어드민 API")
@RestController
@RequestMapping("/v1/projects/requests")
class ProjectEditRequestController(
    private val queryProjectEditRequestService: QueryProjectEditRequestService,
    private val acceptProjectRequestService: AcceptProjectRequestService,
    private val rejectProjectRequestService: RejectProjectRequestService,
) {
    @Operation(summary = "프로젝트 신청 목록 조회", description = "상태 조건에 맞는 프로젝트 신청 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
        ],
    )
    @GetMapping
    fun getProjectRequests(
        @Valid @ModelAttribute queryReq: QueryProjectEditRequestReqDto,
    ): ProjectEditRequestListResDto = queryProjectEditRequestService.execute(queryReq)

    @Operation(summary = "프로젝트 신청 상세 조회", description = "프로젝트 신청 단건의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "404", description = "신청 내역을 찾을 수 없음", content = [Content()]),
        ],
    )
    @GetMapping("/{requestId}")
    fun getProjectRequest(
        @Parameter(description = "신청 ID") @PathVariable requestId: Long,
    ): ProjectEditRequestResDto = queryProjectEditRequestService.executeById(requestId)

    @Operation(summary = "프로젝트 신청 수락", description = "신청 내용을 공식 프로젝트 데이터에 반영합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수락 성공"),
            ApiResponse(responseCode = "404", description = "신청 내역을 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 처리된 신청이거나 중복된 프로젝트 이름", content = [Content()]),
        ],
    )
    @PostMapping("/{requestId}/accept")
    fun acceptProjectRequest(
        @Parameter(description = "신청 ID") @PathVariable requestId: Long,
    ): ProjectResDto = acceptProjectRequestService.execute(requestId)

    @Operation(summary = "프로젝트 신청 거절", description = "거절 사유와 함께 신청을 거절합니다. 기존 프로젝트 데이터는 변경되지 않습니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "거절 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (거절 사유 누락)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "신청 내역을 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 처리된 신청", content = [Content()]),
        ],
    )
    @PostMapping("/{requestId}/reject")
    fun rejectProjectRequest(
        @Parameter(description = "신청 ID") @PathVariable requestId: Long,
        @RequestBody @Valid reqDto: RejectProjectRequestReqDto,
    ) = rejectProjectRequestService.execute(requestId, reqDto)
}
