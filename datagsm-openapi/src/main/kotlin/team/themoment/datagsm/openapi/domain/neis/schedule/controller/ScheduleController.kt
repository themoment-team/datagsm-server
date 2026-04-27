package team.themoment.datagsm.openapi.domain.neis.schedule.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.auth.entity.constant.ApiKeyScope
import team.themoment.datagsm.common.domain.neis.dto.schedule.request.QueryScheduleReqDto
import team.themoment.datagsm.common.domain.neis.dto.schedule.response.ScheduleResDto
import team.themoment.datagsm.openapi.domain.neis.schedule.service.SearchScheduleService
import team.themoment.datagsm.openapi.global.security.annotation.RequireScope

@Tag(name = "Schedule", description = "학사일정 정보 조회 API")
@RestController
@RequestMapping("/v1/neis/schedules")
class ScheduleController(
    private val searchScheduleService: SearchScheduleService,
) {
    @Operation(
        summary = "학사일정 정보 검색",
        description = "학사일정 정보를 검색합니다. date, fromDate, toDate 파라미터로 기간 조회 가능합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (검증 실패)",
                content = [Content()],
            ),
        ],
    )
    @RequireScope(ApiKeyScope.NEIS_READ)
    @GetMapping
    fun searchSchedules(
        @Valid @ModelAttribute queryReq: QueryScheduleReqDto,
    ): ScheduleResDto = searchScheduleService.execute(queryReq)
}
