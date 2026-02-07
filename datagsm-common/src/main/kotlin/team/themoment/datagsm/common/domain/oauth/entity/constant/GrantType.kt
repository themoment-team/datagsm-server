package team.themoment.datagsm.common.domain.oauth.entity.constant

import team.themoment.datagsm.common.domain.oauth.exception.OAuthException

enum class GrantType(
    val value: String,
) {
    AUTHORIZATION_CODE("authorization_code"),
    REFRESH_TOKEN("refresh_token"),
    CLIENT_CREDENTIALS("client_credentials"),
    ;

    companion object {
        fun from(value: String): GrantType =
            entries.find { it.value == value }
                ?: throw OAuthException.UnsupportedGrantType("The grant_type '$value' is not supported")
    }
}
