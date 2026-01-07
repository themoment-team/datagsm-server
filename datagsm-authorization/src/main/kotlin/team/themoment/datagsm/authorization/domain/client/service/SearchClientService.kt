package team.themoment.datagsm.authorization.domain.client.service

import team.themoment.datagsm.authorization.domain.client.dto.response.ClientListResDto

interface SearchClientService {
    fun execute(
        clientName: String?,
        page: Int,
        size: Int,
    ): ClientListResDto
}
