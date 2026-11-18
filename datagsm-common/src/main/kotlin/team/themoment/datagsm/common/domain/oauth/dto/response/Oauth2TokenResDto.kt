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
    @field:Schema(description = "Access Token 유효 기간 (초)", example = "3600")
    val expiresIn: Long,
    @field:JsonProperty("refresh_token")
    @field:Schema(description = "Refresh Token. client_credentials에서는 null이며, 갱신 시 새 토큰으로 교체됩니다.", nullable = true)
    val refreshToken: String?,
    @field:JsonProperty("scope")
    @field:Schema(
        description = "부여된 권한 (공백 구분, appId:scopeName 형식). openid는 프로토콜 값입니다.",
        example = "openid appId:account_read appId:student_read",
    )
    val scope: String,
    @field:JsonProperty("id_token")
    @field:Schema(
        description = "ID Token. authorization_code 교환 시 부여 scope에 openid가 있을 때만 발급하며, 갱신·client_credentials에서는 null입니다.",
        nullable = true,
    )
    val idToken: String? = null,
)
