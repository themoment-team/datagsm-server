package team.themoment.datagsm.common.domain.neis.dto.internal

import com.fasterxml.jackson.annotation.JsonProperty

data class SchoolScheduleInfo(
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
    @field:JsonProperty("DGHT_CRSE_SC_NM")
    val dayNightType: String?,
    @field:JsonProperty("SCHUL_CRSE_SC_NM")
    val schoolCourseType: String?,
    @field:JsonProperty("SBTR_DD_SC_NM")
    val dayCategory: String?,
    @field:JsonProperty("AA_YMD")
    val scheduleDate: String,
    @field:JsonProperty("EVENT_NM")
    val eventName: String,
    @field:JsonProperty("EVENT_CNTNT")
    val eventContent: String?,
    @field:JsonProperty("ONE_GRADE_EVENT_YN")
    val grade1EventYn: String?,
    @field:JsonProperty("TW_GRADE_EVENT_YN")
    val grade2EventYn: String?,
    @field:JsonProperty("THREE_GRADE_EVENT_YN")
    val grade3EventYn: String?,
    @field:JsonProperty("FR_GRADE_EVENT_YN")
    val grade4EventYn: String?,
    @field:JsonProperty("FIV_GRADE_EVENT_YN")
    val grade5EventYn: String?,
    @field:JsonProperty("SIX_GRADE_EVENT_YN")
    val grade6EventYn: String?,
    @field:JsonProperty("LOAD_DTM")
    val loadDateTime: String?,
)
