package team.themoment.datagsm.common.domain.auth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive

data class SearchApiKeyReqDto(
    @field:Positive
    @param:Schema(description = "API 키 ID")
    val id: Long? = null,
    @field:Positive
    @param:Schema(description = "계정 ID")
    val accountId: Long? = null,
    @param:Schema(description = "권한 스코프")
    val scope: String? = null,
    @param:Schema(description = "만료 여부")
    val isExpired: Boolean? = null,
    @param:Schema(description = "갱신 가능 여부")
    val isRenewable: Boolean? = null,
    @field:Min(0)
    @param:Schema(description = "페이지 번호", defaultValue = "0", minimum = "0")
    val page: Int = 0,
    @field:Min(1)
    @field:Max(1000)
    @param:Schema(description = "페이지 크기", defaultValue = "100", minimum = "1", maximum = "1000")
    val size: Int = 100,
)
