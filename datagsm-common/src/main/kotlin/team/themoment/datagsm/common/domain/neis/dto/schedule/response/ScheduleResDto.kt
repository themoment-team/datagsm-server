package team.themoment.datagsm.common.domain.neis.dto.schedule.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.ksp.annotation.SdkExport

@SdkExport
@Schema(description = "학사일정 정보 응답")
data class ScheduleResDto(
    @field:Schema(description = "학사일정 목록")
    val schedules: List<ScheduleInfoResDto>,
)
