package team.themoment.datagsm.domain.neis.schedule.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.domain.neis.schedule.entity.ScheduleRedisEntity
import java.time.LocalDate

@Schema(description = "학사일정 정보 응답")
data class ScheduleResDto(
    @param:Schema(description = "학사일정 ID", example = "7430310_20251215")
    val scheduleId: String,
    @param:Schema(description = "학교 코드", example = "7430310")
    val schoolCode: String,
    @param:Schema(description = "학교명", example = "광주소프트웨어마이스터고등학교")
    val schoolName: String,
    @param:Schema(description = "시도교육청 코드", example = "G10")
    val officeCode: String,
    @param:Schema(description = "시도교육청명", example = "광주광역시교육청")
    val officeName: String,
    @param:Schema(description = "학사일정 날짜", example = "2025-12-15")
    val scheduleDate: LocalDate,
    @param:Schema(description = "학년도", example = "2025")
    val academicYear: String,
    @param:Schema(description = "행사명", example = "기말고사")
    val eventName: String,
    @param:Schema(description = "행사내용", example = "2025학년도 2학기 기말고사")
    val eventContent: String?,
    @param:Schema(description = "수업공제일명 (공휴일/휴업일 등)", example = "휴업일")
    val dayCategory: String?,
    @param:Schema(description = "학교과정명", example = "고등학교")
    val schoolCourseType: String?,
    @param:Schema(description = "주야과정명", example = "주간")
    val dayNightType: String?,
    @param:Schema(description = "해당 학년 목록", example = "[1, 2, 3]")
    val targetGrades: List<Int>,
)
