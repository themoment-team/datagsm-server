package team.themoment.datagsm.authorization.domain.client.service

import team.themoment.datagsm.authorization.domain.client.dto.response.QueryMyClientResDto

interface QueryMyClientService {
    fun execute(): QueryMyClientResDto
}
