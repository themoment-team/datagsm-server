package team.themoment.datagsm.resource.domain.neis.schedule.service

import team.themoment.datagsm.common.domain.neis.dto.schedule.response.ScheduleResDto
import java.time.LocalDate

interface SearchScheduleService {
    fun execute(
        date: LocalDate?,
        fromDate: LocalDate?,
        toDate: LocalDate?,
    ): List<ScheduleResDto>
}
