package team.themoment.datagsm.common.domain.client.entity.constant

import org.springframework.security.core.GrantedAuthority

enum class OAuthScope(
    val scope: String,
    val description: String,
) : GrantedAuthority {
    SELF_READ("self:read", "내 정보 조회"),
    ;

    override fun getAuthority(): String = "SCOPE_$scope"

    companion object {
        fun fromString(scope: String): OAuthScope? = entries.find { it.scope == scope }

        fun getAllScopes(): Set<String> = entries.map { it.scope }.toSet()
    }
}
