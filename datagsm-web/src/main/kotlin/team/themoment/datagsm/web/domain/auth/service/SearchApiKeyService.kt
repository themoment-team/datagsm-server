package team.themoment.datagsm.web.domain.auth.service

import team.themoment.datagsm.common.dto.auth.response.ApiKeySearchResDto

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
