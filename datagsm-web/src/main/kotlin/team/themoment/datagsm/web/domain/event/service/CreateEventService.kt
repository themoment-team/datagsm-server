package team.themoment.datagsm.web.domain.event.service

import team.themoment.datagsm.common.domain.event.dto.request.CreateEventReqDto
import team.themoment.datagsm.common.domain.event.dto.response.CreateEventResDto

interface CreateEventService {
    fun execute(reqDto: CreateEventReqDto): CreateEventResDto
}
