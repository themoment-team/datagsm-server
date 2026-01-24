package team.themoment.datagsm.common.domain.neis.dto.internal

import com.fasterxml.jackson.annotation.JsonProperty

data class NeisScheduleApiResponse(
    @param:JsonProperty("SchoolSchedule")
    val schoolSchedule: List<SchoolScheduleWrapper>?,
)
