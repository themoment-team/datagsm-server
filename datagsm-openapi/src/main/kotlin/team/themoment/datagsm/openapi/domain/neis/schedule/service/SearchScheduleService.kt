package team.themoment.datagsm.openapi.domain.neis.schedule.service

import team.themoment.datagsm.common.domain.neis.dto.schedule.request.QueryScheduleReqDto
import team.themoment.datagsm.common.domain.neis.dto.schedule.response.ScheduleResDto

interface SearchScheduleService {
    fun execute(reqDto: QueryScheduleReqDto): List<ScheduleResDto>
}
