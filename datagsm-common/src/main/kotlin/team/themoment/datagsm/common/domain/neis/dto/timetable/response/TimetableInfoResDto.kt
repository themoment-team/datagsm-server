package team.themoment.datagsm.common.domain.neis.dto.timetable.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "시간표 단건 정보")
data class TimetableInfoResDto(
    @field:Schema(description = "시간표 ID", example = "7380292_20250303_1_1_1")
    val timetableId: String,
    @field:Schema(description = "학교 코드", example = "7380292")
    val schoolCode: String,
    @field:Schema(description = "학교명", example = "광주소프트웨어마이스터고등학교")
    val schoolName: String,
    @field:Schema(description = "시도교육청 코드", example = "F10")
    val officeCode: String,
    @field:Schema(description = "시도교육청명", example = "광주광역시교육청")
    val officeName: String,
    @field:Schema(description = "시간표 날짜", example = "2025-03-03")
    val timetableDate: LocalDate,
    @field:Schema(description = "학년도", example = "2025")
    val academicYear: String,
    @field:Schema(description = "학기", example = "1")
    val semester: String?,
    @field:Schema(description = "학년", example = "1")
    val grade: Int,
    @field:Schema(description = "반", example = "1")
    val classNum: Int,
    @field:Schema(description = "교시", example = "1")
    val period: Int,
    @field:Schema(description = "과목명", example = "국어")
    val subject: String?,
)
