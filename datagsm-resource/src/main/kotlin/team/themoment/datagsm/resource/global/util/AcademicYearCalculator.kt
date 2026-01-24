package team.themoment.datagsm.resource.global.util

import java.time.LocalDate

object AcademicYearCalculator {
    private const val ACADEMIC_YEAR_START_MONTH = 3
    private const val ACADEMIC_YEAR_START_DAY = 1

    data class AcademicYearPeriod(
        val academicYear: Int,
        val startDate: LocalDate,
        val endDate: LocalDate,
    )

    fun getCurrentAcademicYearPeriod(referenceDate: LocalDate = LocalDate.now()): AcademicYearPeriod {
        val year = referenceDate.year
        val month = referenceDate.monthValue

        val academicYear = if (month < ACADEMIC_YEAR_START_MONTH) year - 1 else year

        val startDate = LocalDate.of(academicYear, ACADEMIC_YEAR_START_MONTH, ACADEMIC_YEAR_START_DAY)
        val endDate = calculateEndOfAcademicSchedule(academicYear + 1)

        return AcademicYearPeriod(
            academicYear = academicYear,
            startDate = startDate,
            endDate = endDate,
        )
    }

    private fun calculateEndOfAcademicSchedule(year: Int): LocalDate =
        LocalDate.of(year, ACADEMIC_YEAR_START_MONTH, ACADEMIC_YEAR_START_DAY).minusDays(1)
}
