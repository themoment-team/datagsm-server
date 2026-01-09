package team.themoment.datagsm.web.domain.client.service

import team.themoment.datagsm.web.domain.client.dto.request.ModifyClientReqDto
import team.themoment.datagsm.web.domain.client.dto.response.ClientResDto

interface ModifyClientService {
    fun execute(
        clientId: String,
        reqDto: ModifyClientReqDto,
    ): ClientResDto
}
