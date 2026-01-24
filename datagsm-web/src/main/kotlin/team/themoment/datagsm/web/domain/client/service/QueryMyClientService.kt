package team.themoment.datagsm.web.domain.client.service

import team.themoment.datagsm.common.domain.client.dto.response.ClientListResDto

interface QueryMyClientService {
    fun execute(
        page: Int,
        size: Int,
    ): ClientListResDto
}
