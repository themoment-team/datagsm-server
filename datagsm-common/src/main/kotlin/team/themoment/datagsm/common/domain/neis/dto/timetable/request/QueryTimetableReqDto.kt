package team.themoment.datagsm.common.domain.neis.dto.timetable.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.AssertTrue
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class QueryTimetableReqDto(
    @param:Schema(description = "학년 (1~3)")
    val grade: Int,
    @param:Schema(description = "반 (1~4)")
    val classNum: Int,
    @param:Schema(description = "특정 날짜 (YYYY-MM-DD)")
    @param:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val date: LocalDate? = null,
    @param:Schema(description = "시작 날짜 (YYYY-MM-DD)")
    @param:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val startDate: LocalDate? = null,
    @param:Schema(description = "종료 날짜 (YYYY-MM-DD)")
    @param:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val endDate: LocalDate? = null,
) {
    @AssertTrue(message = "date와 startDate/endDate는 동시에 사용할 수 없습니다.")
    fun isValidDateCombination(): Boolean {
        if (date != null && (startDate != null || endDate != null)) {
            return false
        }
        return true
    }

    @AssertTrue(message = "날짜 조건이 필요합니다. date 또는 startDate/endDate를 입력하세요.")
    fun isDateRequired(): Boolean = date != null || startDate != null || endDate != null

    @AssertTrue(message = "startDate는 endDate보다 이전이거나 같아야 합니다.")
    fun isValidDateRange(): Boolean {
        if (startDate != null && endDate != null) {
            return !startDate.isAfter(endDate)
        }
        return true
    }

    @AssertTrue(message = "최대 조회 기간은 365일입니다.")
    fun isValidDateRangePeriod(): Boolean {
        if (startDate != null && endDate != null) {
            val daysBetween = ChronoUnit.DAYS.between(startDate, endDate)
            return daysBetween <= 365
        }
        return true
    }
}
