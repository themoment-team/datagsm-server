package team.themoment.datagsm.common.dto.neis.internal

import com.fasterxml.jackson.annotation.JsonProperty

data class ApiResult(
    @param:JsonProperty("CODE")
    val code: String?,
    @param:JsonProperty("MESSAGE")
    val message: String?,
)
