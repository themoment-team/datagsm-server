package team.themoment.datagsm.common.domain.oauth.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class Oauth2TokenResDto(
    @field:JsonProperty("access_token")
    @field:Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val accessToken: String,
    @field:JsonProperty("token_type")
    @field:Schema(description = "Token Type", example = "Bearer")
    val tokenType: String = "Bearer",
    @field:JsonProperty("expires_in")
    @field:Schema(description = "Expires in seconds", example = "3600")
    val expiresIn: Long,
    @field:JsonProperty("refresh_token")
    @field:Schema(description = "Refresh Token (선택)", example = "tGzv3JOkF0XG5Qx2TlKWIA")
    val refreshToken: String?,
    @field:JsonProperty("scope")
    @field:Schema(description = "Granted scopes (space-separated)", example = "self:read self:write")
    val scope: String,
)
