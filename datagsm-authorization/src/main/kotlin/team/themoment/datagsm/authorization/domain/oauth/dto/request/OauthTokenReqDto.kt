package team.themoment.datagsm.authorization.domain.oauth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class OauthTokenReqDto(
    @field:NotBlank(message = "인증 코드는 필수입니다.")
    @param:Schema(description = "OAuth 인증 코드", example = "abc123xyz")
    val code: String,
    @field:NotBlank(message = "Client Secret은 필수입니다.")
    @param:Schema(description = "클라이언트 시크릿", example = "secret-key-123")
    val clientSecret: String,
)
