package team.themoment.datagsm.common.dto.neis.internal

import com.fasterxml.jackson.annotation.JsonProperty

data class SchoolScheduleInfo(
    @param:JsonProperty("ATPT_OFCDC_SC_CODE")
    val officeCode: String,
    @param:JsonProperty("ATPT_OFCDC_SC_NM")
    val officeName: String,
    @param:JsonProperty("SD_SCHUL_CODE")
    val schoolCode: String,
    @param:JsonProperty("SCHUL_NM")
    val schoolName: String,
    @param:JsonProperty("AY")
    val academicYear: String,
    @param:JsonProperty("DGHT_CRSE_SC_NM")
    val dayNightType: String?,
    @param:JsonProperty("SCHUL_CRSE_SC_NM")
    val schoolCourseType: String?,
    @param:JsonProperty("SBTR_DD_SC_NM")
    val dayCategory: String?,
    @param:JsonProperty("AA_YMD")
    val scheduleDate: String,
    @param:JsonProperty("EVENT_NM")
    val eventName: String,
    @param:JsonProperty("EVENT_CNTNT")
    val eventContent: String?,
    @param:JsonProperty("ONE_GRADE_EVENT_YN")
    val grade1EventYn: String?,
    @param:JsonProperty("TW_GRADE_EVENT_YN")
    val grade2EventYn: String?,
    @param:JsonProperty("THREE_GRADE_EVENT_YN")
    val grade3EventYn: String?,
    @param:JsonProperty("FR_GRADE_EVENT_YN")
    val grade4EventYn: String?,
    @param:JsonProperty("FIV_GRADE_EVENT_YN")
    val grade5EventYn: String?,
    @param:JsonProperty("SIX_GRADE_EVENT_YN")
    val grade6EventYn: String?,
    @param:JsonProperty("LOAD_DTM")
    val loadDateTime: String?,
)
