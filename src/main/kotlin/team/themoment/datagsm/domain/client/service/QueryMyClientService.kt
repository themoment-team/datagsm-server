package team.themoment.datagsm.domain.client.service

import team.themoment.datagsm.domain.client.dto.res.ClientListResDto

interface QueryMyClientService {
    fun execute(): ClientListResDto
}
