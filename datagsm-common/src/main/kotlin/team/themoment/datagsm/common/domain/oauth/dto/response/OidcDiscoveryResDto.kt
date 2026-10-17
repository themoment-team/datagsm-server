package team.themoment.datagsm.common.domain.oauth.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 * OIDC Discovery 문서. SP가 issuer만 설정하면 나머지 엔드포인트를 스스로 찾게 해준다.
 * 필드명은 OpenID Connect Discovery 1.0 규격이 정한 snake_case를 그대로 따른다.
 */
data class OidcDiscoveryResDto(
    @field:JsonProperty("issuer")
    @field:Schema(description = "발급자 식별자")
    val issuer: String,
    @field:JsonProperty("authorization_endpoint")
    @field:Schema(description = "인가 엔드포인트")
    val authorizationEndpoint: String,
    @field:JsonProperty("token_endpoint")
    @field:Schema(description = "토큰 엔드포인트")
    val tokenEndpoint: String,
    @field:JsonProperty("jwks_uri")
    @field:Schema(description = "JWK Set 엔드포인트")
    val jwksUri: String,
    @field:JsonProperty("userinfo_endpoint")
    @field:Schema(description = "UserInfo 엔드포인트")
    val userinfoEndpoint: String,
    @field:JsonProperty("end_session_endpoint")
    @field:Schema(description = "로그아웃 엔드포인트")
    val endSessionEndpoint: String,
    @field:JsonProperty("response_types_supported")
    val responseTypesSupported: List<String>,
    @field:JsonProperty("grant_types_supported")
    val grantTypesSupported: List<String>,
    @field:JsonProperty("subject_types_supported")
    val subjectTypesSupported: List<String>,
    @field:JsonProperty("id_token_signing_alg_values_supported")
    val idTokenSigningAlgValuesSupported: List<String>,
    @field:JsonProperty("code_challenge_methods_supported")
    val codeChallengeMethodsSupported: List<String>,
    @field:JsonProperty("scopes_supported")
    val scopesSupported: List<String>,
    @field:JsonProperty("token_endpoint_auth_methods_supported")
    val tokenEndpointAuthMethodsSupported: List<String>,
    @field:JsonProperty("claims_supported")
    val claimsSupported: List<String>,
)
