package team.themoment.datagsm.common.domain.client.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class CreateClientResDto(
    @field:Schema(description = "생성된 클라이언트 ID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    val clientId: String,
    @field:Schema(description = "생성된 클라이언트 Secret의 UUID 원문 (생성 시에만 반환되며 서버에는 해시로 저장)", example = "c4f839e2-5747-4a55-83c8-a9c583d7b905")
    val clientSecret: String,
    @field:Schema(description = "클라이언트 이름", example = "My OAuth Client")
    val clientName: String,
    @field:Schema(description = "서비스 명칭", example = "DataGSM")
    val serviceName: String,
    @field:Schema(
        description = "리다이렉트 URL 목록 (생성 응답 필드명은 redirectUrls, 조회·수정 응답은 redirectUrl)",
        example = "[\"https://example.com/callback\"]",
    )
    val redirectUrls: Set<String>,
    @field:Schema(description = "OAuth 권한 범위 목록", example = "[\"a1b2c3d4-e5f6-7890-abcd-ef1234567890:profile\"]")
    val scopes: Set<String>,
)
