package team.themoment.datagsm.common.domain.neis.dto.meal.response

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.neis.meal.entity.constant.MealType
import java.time.LocalDate

@Schema(description = "급식 정보 응답")
data class MealResDto(
    @param:Schema(description = "급식 ID", example = "7430310_20251215_2")
    val mealId: String,
    @param:Schema(description = "학교 코드", example = "7430310")
    val schoolCode: String,
    @param:Schema(description = "학교명", example = "광주소프트웨어마이스터고등학교")
    val schoolName: String,
    @param:Schema(description = "시도교육청 코드", example = "G10")
    val officeCode: String,
    @param:Schema(description = "시도교육청명", example = "광주광역시교육청")
    val officeName: String,
    @param:Schema(description = "급식 날짜", example = "2025-12-15")
    @param:JsonFormat(pattern = "yyyy-MM-dd")
    val mealDate: LocalDate,
    @param:Schema(description = "급식 타입 (BREAKFAST, LUNCH, DINNER)", example = "LUNCH")
    val mealType: MealType,
    @param:Schema(description = "급식 메뉴 목록", example = "[\"쌀밥\", \"김치찌개\", \"돈까스\", \"배추김치\"]")
    val mealMenu: List<String>,
    @param:Schema(description = "알레르기 정보 목록", example = "[\"1\", \"2\", \"5\", \"6\"]")
    val mealAllergyInfo: List<String>?,
    @param:Schema(description = "칼로리 정보", example = "650.8 Kcal")
    val mealCalories: String?,
    @param:Schema(description = "원산지 정보", example = "쌀:국내산 돼지고기:국내산 배추김치:국내산")
    val originInfo: String?,
    @param:Schema(description = "영양 정보", example = "단백질:25.0g 지방:15.0g 탄수화물:90.0g")
    val nutritionInfo: String?,
    @param:Schema(description = "급식 인원수", example = "450")
    val mealServeCount: Int?,
)
