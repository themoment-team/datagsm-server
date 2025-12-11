package team.themoment.datagsm.domain.client.service

import team.themoment.datagsm.domain.client.dto.req.ModifyClientReqDto
import team.themoment.datagsm.domain.client.dto.res.ClientResDto

interface ModifyClientService {
    fun execute(
        clientId: String,
        reqDto: ModifyClientReqDto,
    ): ClientResDto
}
