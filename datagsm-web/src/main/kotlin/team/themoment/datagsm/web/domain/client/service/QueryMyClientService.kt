package team.themoment.datagsm.web.domain.client.service

import team.themoment.datagsm.web.domain.client.dto.response.QueryMyClientResDto

interface QueryMyClientService {
    fun execute(): QueryMyClientResDto
}
