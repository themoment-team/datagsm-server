package team.themoment.datagsm.web.domain.event.service

import team.themoment.datagsm.common.domain.event.dto.response.EventListResDto

interface QueryEventService {
    fun execute(): EventListResDto
}
