package team.themoment.datagsm.domain.client.service

import team.themoment.datagsm.domain.client.dto.response.ClientListResDto

interface QueryMyClientService {
    fun execute(): ClientListResDto
}
