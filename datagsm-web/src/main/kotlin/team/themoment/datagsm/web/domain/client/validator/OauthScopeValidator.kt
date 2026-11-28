package team.themoment.datagsm.web.domain.client.validator

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.application.repository.OAuthScopeJpaRepository
import team.themoment.sdk.exception.ExpectedException

@Component
class OauthScopeValidator(
    private val oauthScopeJpaRepository: OAuthScopeJpaRepository,
) {
    fun validate(requestedScopes: Set<String>) {
        if (requestedScopes.isEmpty()) return

        val applicationIds = requestedScopes.map { it.substringBefore(':') }.toSet()
        val existingScopes =
            oauthScopeJpaRepository
                .findAllByApplicationIdIn(applicationIds)
                .map { "${it.application.id}:${it.scopeName}" }
                .toSet()

        if (!existingScopes.containsAll(requestedScopes)) {
            throw ExpectedException("허용되지 않는 OAuth 권한이 포함되어 있습니다.", HttpStatus.BAD_REQUEST)
        }
    }
}
