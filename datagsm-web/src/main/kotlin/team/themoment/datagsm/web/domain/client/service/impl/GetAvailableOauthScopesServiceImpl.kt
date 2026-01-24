package team.themoment.datagsm.web.domain.client.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.auth.dto.response.ApiScopeGroupListResDto
import team.themoment.datagsm.common.domain.auth.dto.response.ApiScopeResDto
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.web.domain.client.service.GetAvailableOauthScopesService

@Service
class GetAvailableOauthScopesServiceImpl : GetAvailableOauthScopesService {
    override fun execute(): ApiScopeGroupListResDto {
        val allScopes = OAuthScope.entries
        val grouped = OAuthScope.groupByCategory(allScopes)

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
