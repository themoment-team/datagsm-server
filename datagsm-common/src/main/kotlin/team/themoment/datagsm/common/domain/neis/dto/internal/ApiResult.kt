package team.themoment.datagsm.common.domain.neis.dto.internal

import com.fasterxml.jackson.annotation.JsonProperty

data class ApiResult(
    @param:JsonProperty("CODE")
    val code: String?,
    @param:JsonProperty("MESSAGE")
    val message: String?,
)
