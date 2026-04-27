package team.themoment.datagsm.common.domain.neis.dto.internal

import com.fasterxml.jackson.annotation.JsonProperty

data class NeisTimetableApiResponse(
    @field:JsonProperty("hisTimetable")
    val hisTimetable: List<HisTimetableWrapper>?,
)
