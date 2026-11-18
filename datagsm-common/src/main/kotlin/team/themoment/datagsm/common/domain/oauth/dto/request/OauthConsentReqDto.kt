package team.themoment.datagsm.common.domain.oauth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class OauthConsentReqDto(
    @field:NotBlank(message = "토큰은 필수입니다.")
    @param:Schema(description = "인증 시작 시 발급된 세션 토큰", requiredMode = Schema.RequiredMode.REQUIRED)
    val token: String,
    @param:Schema(
        description = "동의 여부. false여도 HTTP 200이며 access_denied가 포함된 redirectUrl을 본문으로 반환합니다.",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val approved: Boolean,
)
