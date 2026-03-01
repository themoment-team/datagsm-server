package team.themoment.datagsm.common.domain.oauth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class OauthAuthorizeReqDto(
    @param:Schema(description = "클라이언트 ID", example = "my-client-id", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "client_id는 필수입니다.")
    val clientId: String?,
    @param:Schema(description = "리다이렉트 URI", example = "https://example.com/callback", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "redirect_uri는 필수입니다.")
    val redirectUri: String?,
    @param:Schema(description = "응답 타입 (code 고정)", example = "code", defaultValue = "code")
    val responseType: String? = "code",
    @param:Schema(description = "상태 값 (CSRF 방지용)", example = "random-state-value", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val state: String? = null,
    @param:Schema(
        description = "PKCE code challenge",
        example = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val codeChallenge: String? = null,
    @param:Schema(
        description = "PKCE code challenge 메서드 (S256 or plain)",
        example = "S256",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val codeChallengeMethod: String? = null,
)
