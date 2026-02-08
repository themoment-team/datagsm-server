package team.themoment.datagsm.common.domain.neis.dto.schedule.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "학사일정 정보 응답")
data class ScheduleResDto(
    @field:Schema(description = "학사일정 ID", example = "7430310_20251215")
    val scheduleId: String,
    @field:Schema(description = "학교 코드", example = "7430310")
    val schoolCode: String,
    @field:Schema(description = "학교명", example = "광주소프트웨어마이스터고등학교")
    val schoolName: String,
    @field:Schema(description = "시도교육청 코드", example = "G10")
    val officeCode: String,
    @field:Schema(description = "시도교육청명", example = "광주광역시교육청")
    val officeName: String,
    @field:Schema(description = "학사일정 날짜", example = "2025-12-15")
    val scheduleDate: LocalDate,
    @field:Schema(description = "학년도", example = "2025")
    val academicYear: String,
    @field:Schema(description = "행사명", example = "기말고사")
    val eventName: String,
    @field:Schema(description = "행사내용", example = "2025학년도 2학기 기말고사")
    val eventContent: String?,
    @field:Schema(description = "수업공제일명 (공휴일/휴업일 등)", example = "휴업일")
    val dayCategory: String?,
    @field:Schema(description = "학교과정명", example = "고등학교")
    val schoolCourseType: String?,
    @field:Schema(description = "주야과정명", example = "주간")
    val dayNightType: String?,
    @field:Schema(description = "해당 학년 목록", example = "[1, 2, 3]")
    val targetGrades: List<Int>,
)
