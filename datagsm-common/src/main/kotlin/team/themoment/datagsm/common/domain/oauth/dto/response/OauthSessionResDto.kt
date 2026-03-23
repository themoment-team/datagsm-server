package team.themoment.datagsm.common.domain.oauth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class OauthSessionResDto(
    @field:Schema(description = "서비스 이름")
    val serviceName: String,
    @field:Schema(description = "세션 만료 시각")
    val expiresAt: Long,
    @field:Schema(description = "클라이언트가 요청한 OAuth Scope 목록")
    val requestedScopes: List<String>,
)
