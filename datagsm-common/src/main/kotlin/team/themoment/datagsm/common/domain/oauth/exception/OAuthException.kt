package team.themoment.datagsm.common.domain.oauth.exception

import org.springframework.http.HttpStatus

sealed class OAuthException(
    val error: String,
    val errorDescription: String,
    val httpStatus: HttpStatus,
) : RuntimeException(errorDescription) {
    class InvalidRequest(
        description: String,
    ) : OAuthException("invalid_request", description, HttpStatus.BAD_REQUEST)

    class InvalidClient(
        description: String = "클라이언트 인증에 실패했습니다.",
    ) : OAuthException("invalid_client", description, HttpStatus.UNAUTHORIZED)

    class InvalidGrant(
        description: String,
    ) : OAuthException("invalid_grant", description, HttpStatus.BAD_REQUEST)

    class UnauthorizedClient(
        description: String,
    ) : OAuthException("unauthorized_client", description, HttpStatus.BAD_REQUEST)

    class UnsupportedGrantType(
        description: String = "지원하지 않는 grant_type입니다.",
    ) : OAuthException("unsupported_grant_type", description, HttpStatus.BAD_REQUEST)

    class InvalidScope(
        description: String,
    ) : OAuthException("invalid_scope", description, HttpStatus.BAD_REQUEST)
}
