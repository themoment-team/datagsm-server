package team.themoment.datagsm.web.domain.event.service

import team.themoment.datagsm.common.domain.event.dto.request.ModifyEventReqDto
import team.themoment.datagsm.common.domain.event.dto.response.EventResDto

interface ModifyEventService {
    fun execute(
        eventId: Long,
        reqDto: ModifyEventReqDto,
    ): EventResDto
}
