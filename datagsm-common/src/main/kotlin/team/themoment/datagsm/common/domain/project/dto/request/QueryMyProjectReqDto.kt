package team.themoment.datagsm.common.domain.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus

data class QueryMyProjectReqDto(
    @param:Schema(description = "신청 상태 필터 (미입력 시 전체 조회)", example = "PENDING")
    val requestStatus: ProjectRequestStatus? = null,
)
