package team.themoment.datagsm.domain.neis.meal.dto.request

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

/**
 * 급식 조회 요청 DTO
 */
@Schema(description = "급식 조회 요청")
data class MealQueryReqDto(
    @Parameter(description = "급식 날짜 (YYYY-MM-DD 형식)", example = "2025-12-15")
    @Schema(description = "급식 날짜", example = "2025-12-15")
    val date: LocalDate? = null,

    @Parameter(description = "급식 시작 날짜 (YYYY-MM-DD 형식)", example = "2025-12-01")
    @Schema(description = "급식 시작 날짜", example = "2025-12-01")
    val fromDate: LocalDate? = null,

    @Parameter(description = "급식 종료 날짜 (YYYY-MM-DD 형식)", example = "2025-12-31")
    @Schema(description = "급식 종료 날짜", example = "2025-12-31")
    val toDate: LocalDate? = null,
)
