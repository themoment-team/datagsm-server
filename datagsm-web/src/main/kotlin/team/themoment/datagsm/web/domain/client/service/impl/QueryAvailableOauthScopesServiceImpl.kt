package team.themoment.datagsm.web.domain.client.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.common.domain.client.dto.response.OAuthScopeListResDto
import team.themoment.datagsm.common.domain.client.dto.response.OAuthScopeResDto
import team.themoment.datagsm.web.domain.client.service.QueryAvailableOauthScopesService

@Service
class QueryAvailableOauthScopesServiceImpl(
    private val applicationJpaRepository: ApplicationJpaRepository,
) : QueryAvailableOauthScopesService {
    override fun execute(): OAuthScopeListResDto {
        val scopes =
            applicationJpaRepository
                .findAllByEager()
                .flatMap { application ->
                    application.oauthScopes.map { scope ->
                        OAuthScopeResDto(
                            scope = "${application.id}:${scope.scopeName}",
                            description = scope.description,
                            applicationName = application.name,
                        )
                    }
                }
        return OAuthScopeListResDto(list = scopes)
    }
}
