package team.themoment.datagsm.common.domain.neis.dto.internal

import com.fasterxml.jackson.annotation.JsonProperty

data class MealServiceDietInfo(
    @field:JsonProperty("ATPT_OFCDC_SC_CODE")
    val officeCode: String,
    @field:JsonProperty("ATPT_OFCDC_SC_NM")
    val officeName: String,
    @field:JsonProperty("SD_SCHUL_CODE")
    val schoolCode: String,
    @field:JsonProperty("SCHUL_NM")
    val schoolName: String,
    @field:JsonProperty("MMEAL_SC_CODE")
    val mealTypeCode: String,
    @field:JsonProperty("MMEAL_SC_NM")
    val mealTypeName: String,
    @field:JsonProperty("MLSV_YMD")
    val mealDate: String,
    @field:JsonProperty("MLSV_FGR")
    val mealServeCount: String?,
    @field:JsonProperty("DDISH_NM")
    val dishName: String?,
    @field:JsonProperty("ORPLC_INFO")
    val originInfo: String?,
    @field:JsonProperty("CAL_INFO")
    val calorieInfo: String?,
    @field:JsonProperty("NTR_INFO")
    val nutritionInfo: String?,
    @field:JsonProperty("MLSV_FROM_YMD")
    val mealFromDate: String?,
    @field:JsonProperty("MLSV_TO_YMD")
    val mealToDate: String?,
)
