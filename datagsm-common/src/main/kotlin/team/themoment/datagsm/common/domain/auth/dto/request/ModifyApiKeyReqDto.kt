package team.themoment.datagsm.common.domain.auth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

data class ModifyApiKeyReqDto(
    @field:NotEmpty(message = "scope는 최소 1개 이상 선택해야 합니다.")
    @param:Schema(
        description = "API 키 권한 범위 목록 (최소 1개, USER는 읽기 범위만 허용)",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "[\"student:read\", \"club:read\"]",
    )
    val scopes: Set<String>,
    @param:Schema(
        description = "API 키 설명 (선택, 생략 또는 null이면 설명 없음)",
        example = "프론트엔드 개발용 API 키",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val description: String? = null,
)
