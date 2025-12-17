package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.domain.auth.dto.response.ApiScopeResDto
import team.themoment.datagsm.domain.auth.entity.constant.ApiScope
import team.themoment.datagsm.domain.auth.service.QueryApiScopeService

@Service
class QueryApiScopeServiceImpl : QueryApiScopeService {
    override fun execute(role: AccountRole): List<ApiScopeResDto> =
        ApiScope.entries
            .filter {
                when (role) {
                    AccountRole.ADMIN -> it.accountRole == AccountRole.USER || it.accountRole == AccountRole.ADMIN
                    AccountRole.USER -> it.accountRole == AccountRole.USER
                    else -> false
                }
            }.map { ApiScopeResDto(it.scope, it.description) }
}
