package team.themoment.datagsm.common.domain.neis.dto.schedule.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.AssertTrue
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class QueryScheduleReqDto(
    @param:Schema(description = "특정 날짜 (YYYY-MM-DD)")
    @param:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val date: LocalDate? = null,
    @param:Schema(description = "시작 날짜 (YYYY-MM-DD)")
    @param:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val fromDate: LocalDate? = null,
    @param:Schema(description = "종료 날짜 (YYYY-MM-DD)")
    @param:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val toDate: LocalDate? = null,
) {
    @AssertTrue(message = "date와 fromDate/toDate는 동시에 사용할 수 없습니다.")
    fun isValidDateCombination(): Boolean {
        if (date != null && (fromDate != null || toDate != null)) {
            return false
        }
        return true
    }

    @AssertTrue(message = "fromDate는 toDate보다 이전이거나 같아야 합니다.")
    fun isValidDateRange(): Boolean {
        if (fromDate != null && toDate != null) {
            return !fromDate.isAfter(toDate)
        }
        return true
    }

    @AssertTrue(message = "최대 조회 기간은 365일입니다.")
    fun isValidDateRangePeriod(): Boolean {
        if (fromDate != null && toDate != null) {
            val daysBetween = ChronoUnit.DAYS.between(fromDate, toDate)
            return daysBetween <= 365
        }
        return true
    }
}
