package team.themoment.datagsm.common.domain.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class ApplicationResDto(
    @field:Schema(description = "Application ID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    val id: String,
    @field:Schema(description = "Application 이름", example = "My Application")
    val name: String,
    @field:Schema(description = "소유자 계정 ID")
    val accountId: Long,
    @field:Schema(description = "Third-party 권한 범위 목록")
    val scopes: List<ScopeResDto>,
) {
    data class ScopeResDto(
        @field:Schema(description = "권한 범위 ID")
        val id: Long,
        @field:Schema(description = "권한 범위 이름", example = "profile")
        val scopeName: String,
        @field:Schema(description = "권한 범위 설명", example = "사용자 프로필 정보 조회")
        val description: String,
    )
}
