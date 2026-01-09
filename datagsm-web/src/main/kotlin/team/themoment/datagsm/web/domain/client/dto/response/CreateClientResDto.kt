package team.themoment.datagsm.web.domain.client.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class CreateClientResDto(
    @param:Schema(description = "생성된 클라이언트 ID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    val clientId: String,
    @param:Schema(description = "생성된 클라이언트 Secret (최초 1회만 반환)", example = "\$2a\$12\$p2631BskS5B4yknxhj4UIuoVg8S8BGCXRcanzBZF/VlASgc9gFhjG")
    val clientSecret: String,
    @param:Schema(description = "클라이언트 이름", example = "My OAuth Client")
    val name: String,
    @param:Schema(description = "리다이렉트 URL 목록", example = "[\"https://example.com/callback\"]")
    val redirectUrls: Set<String>,
)
