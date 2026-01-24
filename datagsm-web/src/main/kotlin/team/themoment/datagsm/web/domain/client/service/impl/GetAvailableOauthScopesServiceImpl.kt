package team.themoment.datagsm.web.domain.client.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.client.dto.response.OAuthScopeGroupListResDto
import team.themoment.datagsm.common.domain.client.dto.response.OAuthScopeResDto
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.web.domain.client.service.GetAvailableOauthScopesService

@Service
class GetAvailableOauthScopesServiceImpl : GetAvailableOauthScopesService {
    override fun execute(): OAuthScopeGroupListResDto {
        val allScopes = OAuthScope.entries
        val grouped = OAuthScope.groupByCategory(allScopes)

        return OAuthScopeGroupListResDto(
            list =
                grouped.map { (categoryDisplayName, scopes) ->
                    OAuthScopeGroupListResDto.OAuthScopeGroupResDto(
                        title = categoryDisplayName,
                        scopes = scopes.map { OAuthScopeResDto(it.scope, it.description) },
                    )
                },
        )
    }
}
