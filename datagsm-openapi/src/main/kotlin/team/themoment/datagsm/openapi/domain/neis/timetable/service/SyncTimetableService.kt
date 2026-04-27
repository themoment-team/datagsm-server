package team.themoment.datagsm.openapi.domain.neis.timetable.service

import java.time.LocalDate

interface SyncTimetableService {
    fun execute(
        fromDate: LocalDate,
        toDate: LocalDate,
    )
}
