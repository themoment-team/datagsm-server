package team.themoment.datagsm.common.domain.neis.dto.internal

import com.fasterxml.jackson.annotation.JsonProperty

data class ApiResult(
    @field:JsonProperty("CODE")
    val code: String?,
    @field:JsonProperty("MESSAGE")
    val message: String?,
)
