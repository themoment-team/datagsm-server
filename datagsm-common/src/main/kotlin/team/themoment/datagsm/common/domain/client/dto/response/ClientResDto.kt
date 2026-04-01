package team.themoment.datagsm.common.domain.client.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class ClientResDto(
    @field:Schema(description = "클라이언트 ID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    val id: String,
    @field:Schema(description = "클라이언트 이름", example = "My OAuth Client")
    val clientName: String,
    @field:Schema(description = "서비스 명칭", example = "DataGSM")
    val serviceName: String,
    @field:Schema(description = "리다이렉트 URL 목록", example = "[\"https://example.com/callback\"]")
    val redirectUrl: Set<String>,
    @field:Schema(description = "허용된 OAuth 권한 범위 목록", example = "[\"self:read\"]")
    val scopes: Set<String>,
)
