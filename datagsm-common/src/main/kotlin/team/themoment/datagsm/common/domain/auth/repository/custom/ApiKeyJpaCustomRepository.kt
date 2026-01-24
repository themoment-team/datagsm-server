package team.themoment.datagsm.common.domain.auth.repository.custom

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.common.domain.auth.entity.ApiKey

interface ApiKeyJpaCustomRepository {
    fun searchApiKeyWithPaging(
        id: Long?,
        accountId: Long?,
        scope: String?,
        isExpired: Boolean?,
        isRenewable: Boolean?,
        pageable: Pageable,
    ): Page<ApiKey>
}
