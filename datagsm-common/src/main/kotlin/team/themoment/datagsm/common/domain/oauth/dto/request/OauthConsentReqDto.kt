package team.themoment.datagsm.common.domain.oauth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class OauthConsentReqDto(
    @field:NotBlank(message = "토큰은 필수입니다.")
    @field:Schema(description = "인증 시작 시 발급된 세션 토큰")
    val token: String,
    @field:Schema(description = "동의 여부. false면 access_denied로 클라이언트에 리다이렉트합니다.")
    val approved: Boolean,
)
