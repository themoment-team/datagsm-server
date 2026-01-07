package team.themoment.datagsm.authorization.domain.client.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

data class ModifyClientReqDto(
    @field:Size(max = 100)
    @param:Schema(description = "클라이언트 이름 (수정할 경우에만 포함)", example = "Updated Client Name", maxLength = 100)
    val name: String? = null,
    @param:Schema(
        description = "리다이렉트 URL 목록 (수정할 경우에만 포함)",
        example = "[\"https://example.com/callback\", \"https://app.example.com/oauth/callback\"]",
    )
    val redirectUrls: Set<String>? = null,
)
