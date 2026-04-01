package team.themoment.datagsm.common.domain.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ModifyOAuthScopeReqDto(
    @field:NotBlank
    @field:Size(max = 100)
    @field:Pattern(
        regexp = "^[a-z0-9_-]+$",
        message = "scopeName은 소문자 영문, 숫자, 언더스코어, 하이픈만 포함할 수 있습니다. (콜론 불가)",
    )
    @param:Schema(description = "권한 범위 이름", example = "profile")
    val scopeName: String,
    @field:NotBlank
    @field:Size(max = 255)
    @param:Schema(description = "권한 범위 설명", example = "사용자 프로필 정보 조회")
    val description: String,
)
