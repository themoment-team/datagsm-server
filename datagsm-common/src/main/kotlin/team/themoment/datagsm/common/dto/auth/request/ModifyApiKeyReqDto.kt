package team.themoment.datagsm.common.dto.auth.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

data class ModifyApiKeyReqDto(
    @field:NotEmpty(message = "scope는 최소 1개 이상 선택해야 합니다.")
    @param:Schema(
        description = "API 키 권한 스코프 목록",
        example = "[\"student:read\", \"club:read\", \"club:write\"]",
    )
    val scopes: Set<String>,
    @param:Schema(description = "API 키 설명 (선택)", example = "프론트엔드 개발용 API 키")
    val description: String? = null,
)
