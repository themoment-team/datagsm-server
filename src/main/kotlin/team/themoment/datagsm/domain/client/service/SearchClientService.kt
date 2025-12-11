package team.themoment.datagsm.domain.client.service

import team.themoment.datagsm.domain.client.dto.res.ClientListResDto

interface SearchClientService {
    fun execute(
        clientName: String?,
        page: Int,
        size: Int,
    ): ClientListResDto
}
