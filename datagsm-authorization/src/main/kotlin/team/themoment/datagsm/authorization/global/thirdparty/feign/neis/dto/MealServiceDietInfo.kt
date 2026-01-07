package team.themoment.datagsm.authorization.global.thirdparty.feign.neis.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class MealServiceDietInfo(
    @param:JsonProperty("ATPT_OFCDC_SC_CODE")
    val officeCode: String,
    @param:JsonProperty("ATPT_OFCDC_SC_NM")
    val officeName: String,
    @param:JsonProperty("SD_SCHUL_CODE")
    val schoolCode: String,
    @param:JsonProperty("SCHUL_NM")
    val schoolName: String,
    @param:JsonProperty("MMEAL_SC_CODE")
    val mealTypeCode: String,
    @param:JsonProperty("MMEAL_SC_NM")
    val mealTypeName: String,
    @param:JsonProperty("MLSV_YMD")
    val mealDate: String,
    @param:JsonProperty("MLSV_FGR")
    val mealServeCount: String?,
    @param:JsonProperty("DDISH_NM")
    val dishName: String?,
    @param:JsonProperty("ORPLC_INFO")
    val originInfo: String?,
    @param:JsonProperty("CAL_INFO")
    val calorieInfo: String?,
    @param:JsonProperty("NTR_INFO")
    val nutritionInfo: String?,
    @param:JsonProperty("MLSV_FROM_YMD")
    val mealFromDate: String?,
    @param:JsonProperty("MLSV_TO_YMD")
    val mealToDate: String?,
)
