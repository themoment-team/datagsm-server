package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.domain.auth.dto.response.ApiScopeGroupListResDto
import team.themoment.datagsm.domain.auth.dto.response.ApiScopeResDto
import team.themoment.datagsm.domain.auth.entity.constant.ApiScope
import team.themoment.datagsm.domain.auth.entity.constant.ApiScopeCategory
import team.themoment.datagsm.domain.auth.service.QueryApiScopeGroupService

@Service
class QueryApiScopeGroupServiceImpl : QueryApiScopeGroupService {
    override fun execute(role: AccountRole): ApiScopeGroupListResDto {
        val filteredScopes =
            ApiScope.entries
                .filter { scope ->
                    scope.category != null &&
                        when (role) {
                            AccountRole.ADMIN -> scope.accountRole == AccountRole.USER || scope.accountRole == AccountRole.ADMIN
                            AccountRole.USER -> scope.accountRole == AccountRole.USER
                            else -> false
                        }
                }

        val grouped = filteredScopes.groupBy { it.category!! }

        return ApiScopeGroupListResDto(
            data =
                grouped.map { (category, scopes) ->
                    ApiScopeGroupListResDto.ApiScopeGroupResDto(
                        title = category.displayName,
                        scopes = scopes.map { ApiScopeResDto(it.scope, it.description) },
                    )
                },
        )
    }
}
