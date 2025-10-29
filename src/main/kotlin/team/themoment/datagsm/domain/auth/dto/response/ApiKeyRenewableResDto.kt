package team.themoment.datagsm.domain.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class ApiKeyRenewableResDto(
    @param:Schema(description = "API 키 갱신 가능 여부", example = "true")
    val renewable: Boolean,
)
