package team.themoment.datagsm.domain.client.service

import team.themoment.datagsm.domain.client.dto.request.CreateClientReqDto
import team.themoment.datagsm.domain.client.dto.response.CreateClientResDto

interface CreateClientService {
    fun execute(reqDto: CreateClientReqDto): CreateClientResDto
}
