package team.themoment.datagsm.domain.auth.dto

import jakarta.validation.constraints.NotBlank

data class OAuthCodeReqDto(
    @field:NotBlank(message = "Authorization Code는 필수입니다.")
    val code: String,
)
