package team.themoment.datagsm.web.domain.client.service

import team.themoment.datagsm.common.dto.client.request.ModifyClientReqDto
import team.themoment.datagsm.common.dto.client.response.ClientResDto

interface ModifyClientService {
    fun execute(
        clientId: String,
        reqDto: ModifyClientReqDto,
    ): ClientResDto
}
