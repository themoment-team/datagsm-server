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
        description =
            "학년/반/날짜 조건으로 시간표 캐시를 조회합니다. grade와 classNum은 필수이며 date, fromDate, toDate 중 하나 이상이 필요합니다. " +
                "date와 fromDate/toDate는 함께 사용할 수 없으며, fromDate와 toDate를 모두 지정하면 시작일은 종료일 이하여야 하고 두 날짜의 차이는 365일 이하여야 합니다. " +
                "fromDate 또는 toDate만 지정하는 단방향 조회도 가능합니다. 학년과 반의 범위 검증은 없으며 일치하는 데이터가 없으면 빈 목록을 반환합니다. (필요 scope: neis:read)",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "필수 값 누락, 날짜 형식 오류 또는 날짜 조건 검증 실패",
                content = [Content()],
            ),
            ApiResponse(responseCode = "401", description = "API 키가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "403", description = "NEIS 조회 scope가 부족함", content = [Content()]),
            ApiResponse(responseCode = "429", description = "API 키의 요청 한도를 초과함", content = [Content()]),
        ],
    )
    @RequireScope(ApiKeyScope.NEIS_READ)
    @GetMapping
    fun searchTimetables(
        @Valid @ModelAttribute queryReq: QueryTimetableReqDto,
    ): TimetableResDto = searchTimetableService.execute(queryReq)
}
