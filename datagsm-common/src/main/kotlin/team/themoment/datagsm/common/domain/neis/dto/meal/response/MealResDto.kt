package team.themoment.datagsm.common.domain.neis.dto.meal.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "급식 정보 응답")
data class MealResDto(
    @field:Schema(description = "급식 목록")
    val meals: List<MealInfoResDto>,
)
