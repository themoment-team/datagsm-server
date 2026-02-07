package team.themoment.datagsm.common.domain.neis.dto.internal

import com.fasterxml.jackson.annotation.JsonProperty

data class ApiHead(
    @field:JsonProperty("list_total_count")
    val listTotalCount: Int?,
    @field:JsonProperty("RESULT")
    val result: ApiResult?,
)
