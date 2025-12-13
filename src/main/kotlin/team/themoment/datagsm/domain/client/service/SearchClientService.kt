package team.themoment.datagsm.domain.client.service

import team.themoment.datagsm.domain.client.dto.response.ClientListResDto

interface SearchClientService {
    fun execute(
        clientName: String?,
        page: Int,
        size: Int,
    ): ClientListResDto
}
