package team.themoment.datagsm.resource.domain.neis.meal.service

import java.time.LocalDate

interface SyncMealService {
    fun execute(
        fromDate: LocalDate,
        toDate: LocalDate,
    )
}
