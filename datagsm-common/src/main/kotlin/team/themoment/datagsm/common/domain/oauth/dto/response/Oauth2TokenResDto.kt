package team.themoment.datagsm.common.domain.oauth.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class Oauth2TokenResDto(
    @param:JsonProperty("access_token")
    @param:Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val accessToken: String,
    @param:JsonProperty("token_type")
    @param:Schema(description = "Token Type", example = "Bearer")
    val tokenType: String = "Bearer",
    @param:JsonProperty("expires_in")
    @param:Schema(description = "Expires in seconds", example = "3600")
    val expiresIn: Long,
    @param:JsonProperty("refresh_token")
    @param:Schema(description = "Refresh Token (선택)", example = "tGzv3JOkF0XG5Qx2TlKWIA")
    val refreshToken: String?,
    @param:JsonProperty("scope")
    @param:Schema(description = "Granted scopes (space-separated)", example = "self:read self:write")
    val scope: String,
)
