package team.themoment.datagsm.authorization.global.thirdparty.feign.neis.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ApiResult(
    @param:JsonProperty("CODE")
    val code: String?,
    @param:JsonProperty("MESSAGE")
    val message: String?,
)
