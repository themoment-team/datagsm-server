package team.themoment.datagsm.domain.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class ApiKeyResDto(
    @param:Schema(description = "생성된 API 키", example = "550e8400-e29b-41d4-a716-446655440000")
    val apiKey: UUID,
)
