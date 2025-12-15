package team.themoment.datagsm.global.thirdparty.feign.neis.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ApiHead(
    @param:JsonProperty("list_total_count")
    val listTotalCount: Int?,
    @param:JsonProperty("RESULT")
    val result: ApiResult?,
)