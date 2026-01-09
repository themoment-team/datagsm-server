package team.themoment.datagsm.common.dto.neis.internal

import com.fasterxml.jackson.annotation.JsonProperty

data class NeisScheduleApiResponse(
    @param:JsonProperty("SchoolSchedule")
    val schoolSchedule: List<SchoolScheduleWrapper>?,
)
