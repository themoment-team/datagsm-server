package team.themoment.datagsm.global.security.checker

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import team.themoment.datagsm.global.security.validator.ScopeValidator

@Component("scopeChecker")
class ScopeChecker {
    fun hasScope(
        authentication: Authentication,
        requiredScope: String,
    ): Boolean {
        val userScopes =
            authentication.authorities
                .filter { it.authority.startsWith("SCOPE_") }
                .map { it.authority.removePrefix("SCOPE_") }
                .toSet()

        return ScopeValidator.hasScope(userScopes, requiredScope)
    }

    fun hasAnyScope(
        authentication: Authentication,
        requiredScopes: Set<String>,
    ): Boolean {
        val userScopes =
            authentication.authorities
                .filter { it.authority.startsWith("SCOPE_") }
                .map { it.authority.removePrefix("SCOPE_") }
                .toSet()

        return ScopeValidator.hasAnyScope(userScopes, requiredScopes)
    }

    fun hasAllScopes(
        authentication: Authentication,
        requiredScopes: Set<String>,
    ): Boolean {
        val userScopes =
            authentication.authorities
                .filter { it.authority.startsWith("SCOPE_") }
                .map { it.authority.removePrefix("SCOPE_") }
                .toSet()

        return ScopeValidator.hasAllScopes(userScopes, requiredScopes)
    }
}
