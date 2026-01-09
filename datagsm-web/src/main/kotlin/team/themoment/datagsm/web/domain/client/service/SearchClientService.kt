package team.themoment.datagsm.web.domain.client.service

import team.themoment.datagsm.common.dto.client.response.ClientListResDto

interface SearchClientService {
    fun execute(
        clientName: String?,
        page: Int,
        size: Int,
    ): ClientListResDto
}
