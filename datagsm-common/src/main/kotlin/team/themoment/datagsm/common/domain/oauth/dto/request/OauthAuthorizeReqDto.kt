package team.themoment.datagsm.common.domain.oauth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Suppress("PropertyName")
data class OauthAuthorizeReqDto(
    @param:Schema(description = "클라이언트 ID", example = "my-client-id", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "client_id는 필수입니다.")
    val client_id: String?,
    @param:Schema(description = "리다이렉트 URI", example = "https://example.com/callback", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "redirect_uri는 필수입니다.")
    val redirect_uri: String?,
    @param:Schema(description = "응답 타입 (code 고정)", example = "code", defaultValue = "code")
    val response_type: String? = "code",
    @param:Schema(description = "상태 값 (CSRF 방지용)", example = "random-state-value", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val state: String? = null,
    @param:Schema(
        description = "PKCE code challenge",
        example = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val code_challenge: String? = null,
    @param:Schema(
        description = "PKCE code challenge 메서드 (S256 or plain)",
        example = "S256",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val code_challenge_method: String? = null,
    @param:Schema(
        description =
            "요청할 OAuth Scope 목록 (공백 구분, appId:scopeName 형식). 미입력 시 기본 scope(appId:account_read appId:student_read)만 부여됩니다. " +
                "student_read/club_read/project_read는 명시적으로 요청해야 합니다. self_read는 하위 호환을 위한 deprecated scope입니다.",
        example = "appId:account_read appId:student_read appId:club_read appId:project_read",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val scope: String? = null,
)
