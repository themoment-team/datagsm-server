package team.themoment.datagsm.common.domain.neis.dto.internal

import com.fasterxml.jackson.annotation.JsonProperty

data class TimetableInfo(
    @field:JsonProperty("ATPT_OFCDC_SC_CODE")
    val officeCode: String,
    @field:JsonProperty("ATPT_OFCDC_SC_NM")
    val officeName: String,
    @field:JsonProperty("SD_SCHUL_CODE")
    val schoolCode: String,
    @field:JsonProperty("SCHUL_NM")
    val schoolName: String,
    @field:JsonProperty("AY")
    val academicYear: String,
    @field:JsonProperty("SEM")
    val semester: String?,
    @field:JsonProperty("ALL_TI_YMD")
    val timetableDate: String,
    @field:JsonProperty("GRADE")
    val grade: String,
    @field:JsonProperty("CLASS_NM")
    val classNum: String,
    @field:JsonProperty("PERIO")
    val period: String,
    @field:JsonProperty("ITRT_CNTNT")
    val subject: String?,
    @field:JsonProperty("LOAD_DTM")
    val loadDateTime: String?,
)
