package team.themoment.datagsm.common.domain.student.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.ksp.annotation.SdkExport

@SdkExport
data class ParticipantCandidateListResDto(
    @field:Schema(description = "참여자로 선택 가능한 학생 목록")
    val students: List<ParticipantCandidateResDto>,
)
