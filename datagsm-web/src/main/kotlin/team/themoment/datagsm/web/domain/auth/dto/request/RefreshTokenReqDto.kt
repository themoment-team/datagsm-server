package team.themoment.datagsm.web.domain.auth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class RefreshTokenReqDto(
    @field:NotBlank(message = "Refresh token은 필수입니다.")
    @param:Schema(description = "갱신 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val refreshToken: String,
)
