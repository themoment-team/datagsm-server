package team.themoment.datagsm.web.domain.client.service

import team.themoment.datagsm.common.dto.client.response.QueryMyClientResDto

interface QueryMyClientService {
    fun execute(): QueryMyClientResDto
}
