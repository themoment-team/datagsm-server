package team.themoment.datagsm.common.domain.oauth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class Oauth2TokenReqDto(
    @field:NotBlank(message = "grant_type은 필수입니다.")
    @param:Schema(description = "Grant Type", example = "authorization_code")
    val grantType: String = "",

    @param:Schema(description = "Client ID", example = "client-123")
    val clientId: String? = null,

    @param:Schema(description = "Client Secret", example = "secret-key-123")
    val clientSecret: String? = null,

    @param:Schema(description = "Authorization Code", example = "abc123xyz")
    val code: String? = null,

    @param:Schema(description = "Redirect URI", example = "https://example.com/callback")
    val redirectUri: String? = null,

    @param:Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val refreshToken: String? = null,

    @param:Schema(description = "OAuth Scopes (space-separated)", example = "read write")
    val scope: String? = null,

    @param:Schema(description = "PKCE Code Verifier", example = "dBjftJeSSVPxgS31dKTHlEpQMZlzvvMpqHN0KT9LM5E")
    val codeVerifier: String? = null,
)
