package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.domain.auth.dto.response.ApiScopeGroupListResDto
import team.themoment.datagsm.domain.auth.dto.response.ApiScopeResDto
import team.themoment.datagsm.domain.auth.entity.constant.ApiScope
import team.themoment.datagsm.domain.auth.service.QueryApiScopeGroupService

@Service
class QueryApiScopeGroupServiceImpl : QueryApiScopeGroupService {
    override fun execute(role: AccountRole): ApiScopeGroupListResDto {
        val filteredScopes =
            ApiScope.entries
                .filter {
                    when (role) {
                        AccountRole.ADMIN -> it.accountRole == AccountRole.USER || it.accountRole == AccountRole.ADMIN
                        AccountRole.USER -> it.accountRole == AccountRole.USER
                        else -> false
                    }
                }
        val grouped =
            filteredScopes.groupBy { scope ->
                scope.scope.substringBefore(':')
            }
        return ApiScopeGroupListResDto(
            data =
                grouped.map { (category, scopes) ->
                    ApiScopeGroupListResDto.ApiScopeGroupResDto(
                        title = "$category:*",
                        description = "$category 모든 권한",
                        scopes =
                            scopes
                                .filter { !it.scope.endsWith(":*") }
                                .map { ApiScopeResDto(it.scope, it.description) },
                    )
                },
        )
    }
}
