package team.themoment.datagsm.domain.client.service

import team.themoment.datagsm.domain.client.dto.response.QueryMyClientResDto

interface QueryMyClientService {
    fun execute(): QueryMyClientResDto
}
