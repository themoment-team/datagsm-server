package team.themoment.datagsm.domain.auth.service

import team.themoment.datagsm.domain.auth.dto.response.ApiKeyRenewableResDto

interface QueryApiKeyRenewableService {
    fun execute(): ApiKeyRenewableResDto
}
