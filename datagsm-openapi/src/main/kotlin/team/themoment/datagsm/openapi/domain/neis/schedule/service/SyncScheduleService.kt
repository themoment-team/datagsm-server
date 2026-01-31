package team.themoment.datagsm.openapi.domain.neis.schedule.service

import java.time.LocalDate

interface SyncScheduleService {
    fun execute(
        fromDate: LocalDate,
        toDate: LocalDate,
    )
}
