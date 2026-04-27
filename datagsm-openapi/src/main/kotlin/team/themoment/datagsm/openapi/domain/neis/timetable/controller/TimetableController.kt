package team.themoment.datagsm.openapi.domain.neis.timetable.controller

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
import team.themoment.datagsm.common.domain.neis.dto.timetable.request.QueryTimetableReqDto
import team.themoment.datagsm.common.domain.neis.dto.timetable.response.TimetableResDto
import team.themoment.datagsm.openapi.domain.neis.timetable.service.SearchTimetableService
import team.themoment.datagsm.openapi.global.security.annotation.RequireScope

@Tag(name = "Timetable", description = "시간표 정보 조회 API")
@RestController
@RequestMapping("/v1/neis/timetables")
class TimetableController(
    private val searchTimetableService: SearchTimetableService,
) {
    @Operation(
        summary = "시간표 정보 검색",
        description = "학년/반/날짜 조건으로 시간표 정보를 검색합니다. grade, classNum은 필수이며 date 또는 startDate/endDate로 날짜 조건을 지정합니다.",
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
    fun searchTimetables(
        @Valid @ModelAttribute queryReq: QueryTimetableReqDto,
    ): TimetableResDto = searchTimetableService.execute(queryReq)
}
