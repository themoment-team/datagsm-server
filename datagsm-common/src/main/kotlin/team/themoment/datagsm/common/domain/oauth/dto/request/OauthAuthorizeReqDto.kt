package team.themoment.datagsm.common.domain.oauth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class OauthAuthorizeReqDto(
    @param:Schema(description = "클라이언트 ID", example = "client-123")
    @field:NotBlank(message = "Client ID는 필수입니다.")
    val clientId: String,
    @param:Schema(description = "리다이렉트 URI", example = "https://example.com/callback")
    @field:NotBlank(message = "Redirect URI는 필수입니다.")
    val redirectUri: String,
    @param:Schema(description = "응답 타입 (code 고정)", example = "code", defaultValue = "code")
    @field:Pattern(regexp = "code", message = "response_type은 'code'여야 합니다.")
    val responseType: String = "code",
    @param:Schema(description = "CSRF 방지용 상태값 (권장)", example = "random-state-123", nullable = true)
    val state: String? = null,
    @param:Schema(description = "PKCE code challenge", example = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM", nullable = true)
    val codeChallenge: String? = null,
    @param:Schema(description = "PKCE challenge method (plain or S256)", example = "S256", nullable = true)
    val codeChallengeMethod: String? = null,
)
