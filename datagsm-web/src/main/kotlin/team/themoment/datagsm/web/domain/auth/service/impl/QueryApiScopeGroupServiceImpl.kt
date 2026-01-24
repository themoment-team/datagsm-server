package team.themoment.datagsm.web.domain.auth.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.auth.dto.response.ApiScopeGroupListResDto
import team.themoment.datagsm.common.domain.auth.dto.response.ApiScopeResDto
import team.themoment.datagsm.common.domain.auth.entity.constant.ApiKeyScope
import team.themoment.datagsm.web.domain.auth.service.QueryApiScopeGroupService

@Service
class QueryApiScopeGroupServiceImpl : QueryApiScopeGroupService {
    override fun execute(role: AccountRole): ApiScopeGroupListResDto {
        val scopesByRole = ApiKeyScope.getScopesByRole(role)
        val grouped = ApiKeyScope.groupByCategory(scopesByRole)

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
