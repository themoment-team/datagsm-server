package team.themoment.datagsm.common.dto.auth.response

import io.swagger.v3.oas.annotations.media.Schema

data class ApiScopeResDto(
    @param:Schema(description = "권한 범위 이름", example = "student:read")
    val scope: String,
    @param:Schema(description = "권한 범위 설명", example = "학생 정보 조회")
    val description: String,
)
