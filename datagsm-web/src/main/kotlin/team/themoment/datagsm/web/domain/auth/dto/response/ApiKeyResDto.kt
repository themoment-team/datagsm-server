package team.themoment.datagsm.web.domain.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class ApiKeyResDto(
    @param:Schema(description = "API 키 ID", example = "1")
    val id: Long,
    @param:Schema(description = "API 키 (생성 시 원본, 조회 시 마스킹)", example = "550e8400-e29b-41d4-a716-446655440000")
    val apiKey: String,
    @param:Schema(description = "API 키 만료일시", example = "2024-12-31T23:59:59")
    val expiresAt: LocalDateTime,
    @param:Schema(description = "API 키 권한 범위 목록", example = "[\"student:read\", \"club:*\"]")
    val scopes: Set<String>,
    @param:Schema(description = "API 키 설명", example = "프론트엔드 개발용 API 키")
    val description: String? = null,
)
