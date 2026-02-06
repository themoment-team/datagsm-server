package team.themoment.datagsm.openapi.domain.neis.meal.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.auth.entity.constant.ApiKeyScope
import team.themoment.datagsm.common.domain.neis.dto.meal.response.MealResDto
import team.themoment.datagsm.openapi.domain.neis.meal.service.SearchMealService
import team.themoment.datagsm.openapi.global.security.annotation.RequireScope
import java.time.LocalDate

@Tag(name = "Meal", description = "급식 정보 조회 API")
@RestController
@RequestMapping("/v1/neis/meals")
class MealController(
    private val searchMealService: SearchMealService,
) {
    @Operation(
        summary = "급식 정보 검색",
        description = "급식 정보를 검색합니다. date, fromDate, toDate 파라미터로 기간 조회 가능합니다.",
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
    //@RequireScope(ApiKeyScope.NEIS_READ)
    @GetMapping
    fun searchMeals(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        fromDate: LocalDate?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        toDate: LocalDate?,
    ): List<MealResDto> =
        searchMealService.execute(
            date = date,
            fromDate = fromDate,
            toDate = toDate,
        )
}
