package team.themoment.datagsm.domain.neis.schedule.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.domain.auth.entity.constant.ApiScope
import team.themoment.datagsm.domain.neis.schedule.dto.response.ScheduleResDto
import team.themoment.datagsm.domain.neis.schedule.service.SearchScheduleService
import team.themoment.datagsm.global.security.annotation.RequireScope
import java.time.LocalDate

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
                description = "잘못된 요청 파라미터",
                content = [Content()],
            ),
        ],
    )
    @RequireScope(ApiScope.NEIS_READ)
    @GetMapping
    fun searchSchedules(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        fromDate: LocalDate?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        toDate: LocalDate?,
    ): List<ScheduleResDto> =
        searchScheduleService.execute(
            date = date,
            fromDate = fromDate,
            toDate = toDate,
        )
}
