package team.themoment.datagsm.domain.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class ApiScopeResDto(
    @param:Schema(description = "스코프 이름", example = "student:read")
    val scope: String,
    @param:Schema(description = "스코프 설명", example = "학생 정보 조회")
    val description: String,
)
