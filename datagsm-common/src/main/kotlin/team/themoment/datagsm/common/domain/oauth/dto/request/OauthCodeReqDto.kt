package team.themoment.datagsm.common.domain.oauth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL

data class OauthCodeReqDto(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "유효한 이메일 형식이어야 합니다.")
    @field:Size(max = 50)
    @param:Schema(description = "사용자 이메일", example = "user@gsm.hs.kr", maxLength = 50)
    val email: String,
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Size(min = 8, max = 100)
    @param:Schema(description = "사용자 비밀번호", example = "password123!", minLength = 8, maxLength = 100)
    val password: String,
    @field:NotBlank(message = "Client ID는 필수입니다.")
    @param:Schema(description = "클라이언트 ID", example = "client-123")
    val clientId: String,
    @field:NotBlank(message = "Redirect URL은 필수입니다.")
    @field:URL(message = "Redirect URL 형식이 올바르지 않습니다.")
    @param:Schema(description = "리다이렉트 URL", example = "https://example.com/callback")
    val redirectUrl: String,
    @param:Schema(description = "요청 스코프 (공백으로 구분)", example = "self:read")
    val scope: String? = null,
    @param:Schema(description = "PKCE Code Challenge", example = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM")
    val codeChallenge: String? = null,
    @param:Schema(description = "PKCE Code Challenge Method", example = "S256")
    val codeChallengeMethod: String? = null,
)
