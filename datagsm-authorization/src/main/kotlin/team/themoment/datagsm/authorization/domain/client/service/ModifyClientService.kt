package team.themoment.datagsm.authorization.domain.client.service

import team.themoment.datagsm.authorization.domain.client.dto.request.ModifyClientReqDto
import team.themoment.datagsm.authorization.domain.client.dto.response.ClientResDto

interface ModifyClientService {
    fun execute(
        clientId: String,
        reqDto: ModifyClientReqDto,
    ): ClientResDto
}
