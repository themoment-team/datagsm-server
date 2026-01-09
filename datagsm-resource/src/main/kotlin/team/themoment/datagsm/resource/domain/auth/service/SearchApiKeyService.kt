package team.themoment.datagsm.resource.domain.auth.service

import team.themoment.datagsm.common.domain.auth.dto.response.ApiKeySearchResDto

interface SearchApiKeyService {
    fun execute(
        id: Long?,
        accountId: Long?,
        scope: String?,
        isExpired: Boolean?,
        isRenewable: Boolean?,
        page: Int,
        size: Int,
    ): ApiKeySearchResDto
}
