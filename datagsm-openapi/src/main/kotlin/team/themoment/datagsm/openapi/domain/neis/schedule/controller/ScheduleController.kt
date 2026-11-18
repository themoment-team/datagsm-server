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
        description =
            "학사일정 캐시를 조회합니다. date는 특정 날짜, fromDate/toDate는 시작일/종료일이며 한쪽 경계만 지정할 수도 있습니다. " +
                "date와 fromDate/toDate는 함께 사용할 수 없으며, fromDate와 toDate를 모두 지정하면 시작일은 종료일 이하여야 하고 두 날짜의 차이는 365일 이하여야 합니다. " +
                "날짜 조건이 없으면 캐시 전체를 반환합니다. 결과는 날짜, 행사명, scheduleId 순으로 정렬됩니다. (필요 scope: neis:read)",
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
    fun searchSchedules(
        @Valid @ModelAttribute queryReq: QueryScheduleReqDto,
    ): ScheduleResDto = searchScheduleService.execute(queryReq)
}
