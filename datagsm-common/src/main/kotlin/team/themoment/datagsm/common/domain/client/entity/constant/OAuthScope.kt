package team.themoment.datagsm.common.domain.client.entity.constant

import org.springframework.security.core.GrantedAuthority

class OAuthScope(
    val applicationId: String,
    val scopeName: String,
    val description: String,
) : GrantedAuthority {
    val scope: String get() = "$applicationId:$scopeName"

    override fun getAuthority(): String = "SCOPE_$scope"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OAuthScope) return false
        return scope == other.scope
    }

    override fun hashCode(): Int = scope.hashCode()

    companion object {
        fun fromScopeString(scopeStr: String): OAuthScope? {
            val colonIdx = scopeStr.indexOf(':')
            if (colonIdx <= 0 || colonIdx == scopeStr.lastIndex) return null
            return OAuthScope(
                applicationId = scopeStr.substring(0, colonIdx),
                scopeName = scopeStr.substring(colonIdx + 1),
                description = "",
            )
        }
    }
}
