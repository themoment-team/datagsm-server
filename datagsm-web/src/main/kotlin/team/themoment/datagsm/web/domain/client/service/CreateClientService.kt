package team.themoment.datagsm.web.domain.client.service

import team.themoment.datagsm.common.dto.client.request.CreateClientReqDto
import team.themoment.datagsm.common.dto.client.response.CreateClientResDto

interface CreateClientService {
    fun execute(reqDto: CreateClientReqDto): CreateClientResDto
}
