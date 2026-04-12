package team.themoment.datagsm.common.domain.neis.dto.timetable.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "시간표 정보 응답")
data class TimetableResDto(
    @field:Schema(description = "시간표 목록")
    val timetables: List<TimetableInfoResDto>,
)
