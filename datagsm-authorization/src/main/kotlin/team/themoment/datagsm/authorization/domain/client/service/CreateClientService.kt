package team.themoment.datagsm.authorization.domain.client.service

import team.themoment.datagsm.authorization.domain.client.dto.request.CreateClientReqDto
import team.themoment.datagsm.authorization.domain.client.dto.response.CreateClientResDto

interface CreateClientService {
    fun execute(reqDto: CreateClientReqDto): CreateClientResDto
}
