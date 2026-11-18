package team.themoment.datagsm.common.domain.oauth.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.client.dto.response.OAuthScopeResDto

data class OauthSessionResDto(
    @field:Schema(description = "서비스 이름")
    val serviceName: String,
    @field:Schema(description = "조회 시각에 설정된 세션 TTL을 더한 시각 (epoch milliseconds). 조회마다 달라지며 실제 남은 TTL이 아닙니다.")
    val expiresAt: Long,
    @field:Schema(description = "클라이언트가 요청한 OAuth Scope 목록")
    val requestedScopes: List<OAuthScopeResDto>,
)
