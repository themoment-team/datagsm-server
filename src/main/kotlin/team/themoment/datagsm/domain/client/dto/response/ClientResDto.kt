package team.themoment.datagsm.domain.client.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class ClientResDto(
    @param:Schema(description = "클라이언트 ID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    val id: String,
    @param:Schema(description = "클라이언트 이름", example = "My OAuth Client")
    val name: String,
    @param:Schema(description = "리다이렉트 URL 목록", example = "[\"https://example.com/callback\"]")
    val redirectUrl: Set<String>,
)
