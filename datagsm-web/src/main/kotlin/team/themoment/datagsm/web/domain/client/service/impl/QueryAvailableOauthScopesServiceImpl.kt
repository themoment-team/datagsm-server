package team.themoment.datagsm.web.domain.client.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.common.domain.client.dto.response.OAuthScopeGroupListResDto
import team.themoment.datagsm.common.domain.client.dto.response.OAuthScopeResDto
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.web.domain.client.service.QueryAvailableOauthScopesService

@Service
class QueryAvailableOauthScopesServiceImpl(
    private val applicationJpaRepository: ApplicationJpaRepository,
) : QueryAvailableOauthScopesService {
    override fun execute(): OAuthScopeGroupListResDto {
        val builtinGroups =
            OAuthScope.groupByCategory(OAuthScope.builtinValues).map { (categoryDisplayName, scopes) ->
                OAuthScopeGroupListResDto.OAuthScopeGroupResDto(
                    title = categoryDisplayName,
                    scopes = scopes.map { OAuthScopeResDto(it.scope, it.description) },
                )
            }

        val thirdPartyGroups =
            applicationJpaRepository
                .findAllByEager()
                .filter { it.thirdPartyScopes.isNotEmpty() }
                .map { application ->
                    OAuthScopeGroupListResDto.OAuthScopeGroupResDto(
                        title = application.name,
                        scopes =
                            application.thirdPartyScopes.map { scope ->
                                OAuthScopeResDto("${application.id}:${scope.scopeName}", scope.description)
                            },
                    )
                }

        return OAuthScopeGroupListResDto(list = builtinGroups + thirdPartyGroups)
    }
}
