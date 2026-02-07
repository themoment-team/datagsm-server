package team.themoment.datagsm.common.domain.oauth.dto.request

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class Oauth2TokenReqDto(
    @field:NotBlank(message = "grant_type은 필수입니다.")
    @param:Schema(description = "Grant Type", example = "authorization_code")
    @param:JsonProperty("grant_type")
    @param:JsonAlias("grantType")
    val grantType: String = "",
    @param:Schema(description = "Client ID", example = "client-123")
    @param:JsonProperty("client_id")
    @param:JsonAlias("clientId")
    val clientId: String? = null,
    @param:Schema(description = "Client Secret", example = "secret-key-123")
    @param:JsonProperty("client_secret")
    @param:JsonAlias("clientSecret")
    val clientSecret: String? = null,
    @param:Schema(description = "Authorization Code", example = "abc123xyz")
    @param:JsonProperty("code")
    val code: String? = null,
    @param:Schema(description = "Redirect URI", example = "https://example.com/callback")
    @param:JsonProperty("redirect_uri")
    @param:JsonAlias("redirectUri")
    val redirectUri: String? = null,
    @param:Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @param:JsonProperty("refresh_token")
    @param:JsonAlias("refreshToken")
    val refreshToken: String? = null,
    @param:Schema(description = "OAuth Scopes (space-separated)", example = "read write")
    @param:JsonProperty("scope")
    val scope: String? = null,
    @param:Schema(description = "PKCE Code Verifier", example = "dBjftJeSSVPxgS31dKTHlEpQMZlzvvMpqHN0KT9LM5E")
    @param:JsonProperty("code_verifier")
    @param:JsonAlias("codeVerifier")
    val codeVerifier: String? = null,
)
