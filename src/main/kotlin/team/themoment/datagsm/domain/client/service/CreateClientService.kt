package team.themoment.datagsm.domain.client.service

import team.themoment.datagsm.domain.client.dto.req.CreateClientReqDto
import team.themoment.datagsm.domain.client.dto.res.CreateClientResDto

interface CreateClientService {
    fun execute(reqDto: CreateClientReqDto): CreateClientResDto
}
