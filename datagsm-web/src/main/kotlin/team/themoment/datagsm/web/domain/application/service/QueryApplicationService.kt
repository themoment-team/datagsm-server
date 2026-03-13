package team.themoment.datagsm.web.domain.application.service

import team.themoment.datagsm.common.domain.application.dto.response.ApplicationResDto

interface QueryApplicationService {
    fun execute(id: String): ApplicationResDto
}
