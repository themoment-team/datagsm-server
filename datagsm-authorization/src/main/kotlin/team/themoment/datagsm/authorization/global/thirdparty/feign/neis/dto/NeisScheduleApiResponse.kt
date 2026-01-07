package team.themoment.datagsm.authorization.global.thirdparty.feign.neis.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class NeisScheduleApiResponse(
    @param:JsonProperty("SchoolSchedule")
    val schoolSchedule: List<SchoolScheduleWrapper>?,
)
