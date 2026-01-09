package team.themoment.datagsm.resource.domain.auth.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.AccountRole
import team.themoment.datagsm.common.domain.account.ApiScope
import team.themoment.datagsm.common.dto.auth.response.ApiScopeGroupListResDto
import team.themoment.datagsm.common.dto.auth.response.ApiScopeResDto
import team.themoment.datagsm.resource.domain.auth.service.QueryApiScopeGroupService

@Service
class QueryApiScopeGroupServiceImpl : QueryApiScopeGroupService {
    override fun execute(role: AccountRole): ApiScopeGroupListResDto {
        val scopesByRole = ApiScope.getScopesByRole(role)
        val grouped = ApiScope.groupByCategory(scopesByRole)

        return ApiScopeGroupListResDto(
            list =
                grouped.map { (categoryDisplayName, scopes) ->
                    ApiScopeGroupListResDto.ApiScopeGroupResDto(
                        title = categoryDisplayName,
                        scopes = scopes.map { ApiScopeResDto(it.scope, it.description) },
                    )
                },
        )
    }
}
