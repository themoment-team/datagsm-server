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
        /**
         * OIDC 표준 scope. 다른 scope와 달리 applicationId 접두어가 없고
         * tb_oauth_scope에도 존재하지 않는다. 권한을 부여하는 값이 아니라
         * "id_token을 함께 발급하라"는 프로토콜 지시자이므로 별도로 취급한다.
         */
        const val OPENID = "openid"

        const val ACCOUNT_READ = "account_read"
        const val STUDENT_READ = "student_read"
        const val SELF_READ = "self_read"
        const val CLUB_READ = "club_read"
        const val PROJECT_READ = "project_read"

        fun authorityOf(
            applicationId: String,
            scopeName: String,
        ): String = "SCOPE_$applicationId:$scopeName"

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
