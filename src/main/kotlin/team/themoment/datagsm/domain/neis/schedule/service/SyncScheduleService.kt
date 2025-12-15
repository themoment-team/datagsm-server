package team.themoment.datagsm.domain.neis.schedule.service

import java.time.LocalDate

interface SyncScheduleService {
    fun execute(
        fromDate: LocalDate,
        toDate: LocalDate,
    )
}
