package team.themoment.datagsm.web.domain.client.service

import team.themoment.datagsm.web.domain.client.dto.request.CreateClientReqDto
import team.themoment.datagsm.web.domain.client.dto.response.CreateClientResDto

interface CreateClientService {
    fun execute(reqDto: CreateClientReqDto): CreateClientResDto
}
