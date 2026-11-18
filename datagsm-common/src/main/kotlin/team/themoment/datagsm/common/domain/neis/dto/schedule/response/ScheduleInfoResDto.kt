package team.themoment.datagsm.common.domain.neis.dto.schedule.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.ksp.annotation.SdkExport
import java.time.LocalDate

@SdkExport
@Schema(description = "학사일정 단건 정보")
data class ScheduleInfoResDto(
    @field:Schema(
        description = "학사일정 ID (학교코드_날짜_행사명·행사내용·수업공제일명·1~6학년 대상 플래그 조합의 SHA-256 앞 16자리)",
        example = "7380292_20251215_3f9a1c2b7e4d8a06",
    )
    val scheduleId: String,
    @field:Schema(description = "학교 코드", example = "7380292")
    val schoolCode: String,
    @field:Schema(description = "학교명", example = "광주소프트웨어마이스터고등학교")
    val schoolName: String,
    @field:Schema(description = "시도교육청 코드", example = "F10")
    val officeCode: String,
    @field:Schema(description = "시도교육청명", example = "광주광역시교육청")
    val officeName: String,
    @field:Schema(description = "학사일정 날짜", example = "2025-12-15")
    val scheduleDate: LocalDate,
    @field:Schema(description = "학년도", example = "2025")
    val academicYear: String,
    @field:Schema(description = "행사명", example = "기말고사")
    val eventName: String,
    @field:Schema(description = "행사내용", example = "2025학년도 2학기 기말고사", nullable = true)
    val eventContent: String?,
    @field:Schema(description = "수업공제일명 (공휴일/휴업일 등)", example = "휴업일", nullable = true)
    val dayCategory: String?,
    @field:Schema(description = "학교과정명", example = "고등학교", nullable = true)
    val schoolCourseType: String?,
    @field:Schema(description = "주야과정명", example = "주간", nullable = true)
    val dayNightType: String?,
    @field:Schema(description = "해당 학년 목록", example = "[1, 2, 3]")
    val targetGrades: List<Int>,
)
