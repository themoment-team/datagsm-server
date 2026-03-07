package team.themoment.datagsm.common.domain.client.entity.constant

import org.springframework.security.core.GrantedAuthority

open class OAuthScope(
    open val scope: String,
    open val description: String,
) : GrantedAuthority {
    override fun getAuthority(): String = "SCOPE_$scope"

    val category: String get() = scope.substringBefore(':')

    val categoryDisplayName: String get() = CATEGORY_DISPLAY_NAMES[category] ?: category

    companion object {
        val SELF_READ = OAuthScope("self:read", "내 정보 조회")
        val builtinValues: List<OAuthScope> = listOf(SELF_READ)
        private val CATEGORY_DISPLAY_NAMES = mapOf("self" to "사용자")

        fun fromString(scope: String): OAuthScope? = builtinValues.find { it.scope == scope }

        fun getAllScopes(): Set<String> = builtinValues.map { it.scope }.toSet()

        fun groupByCategory(scopes: List<OAuthScope>): Map<String, List<OAuthScope>> = scopes.groupBy { it.categoryDisplayName }
    }
}
