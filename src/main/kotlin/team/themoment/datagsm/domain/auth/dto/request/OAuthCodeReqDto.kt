package team.themoment.datagsm.domain.auth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class OAuthCodeReqDto(
    @field:NotBlank(message = "Authorization Code는 필수입니다.")
    @param:Schema(description = "Google OAuth Authorization Code", example = "4/0AY0e-g7...")
    val code: String,
)
