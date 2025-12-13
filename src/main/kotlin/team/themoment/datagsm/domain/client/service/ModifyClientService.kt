package team.themoment.datagsm.domain.client.service

import team.themoment.datagsm.domain.client.dto.request.ModifyClientReqDto
import team.themoment.datagsm.domain.client.dto.response.ClientResDto

interface ModifyClientService {
    fun execute(
        clientId: String,
        reqDto: ModifyClientReqDto,
    ): ClientResDto
}
