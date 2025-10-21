package team.themoment.datagsm.domain.auth.dto

import jakarta.validation.constraints.NotBlank

data class RefreshTokenReqDto(
    @field:NotBlank(message = "Refresh token은 필수입니다.")
    val refreshToken: String,
)
