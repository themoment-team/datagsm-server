package team.themoment.datagsm.common.domain.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus

data class QueryProjectEditRequestReqDto(
    @param:Schema(description = "신청 상태 (미입력 시 대기 중만 조회)", defaultValue = "PENDING")
    val requestStatus: ProjectRequestStatus? = ProjectRequestStatus.PENDING,
    @field:Min(0)
    @param:Schema(description = "페이지 번호", defaultValue = "0", minimum = "0")
    val page: Int = 0,
    @field:Min(1)
    @field:Max(1000)
    @param:Schema(description = "페이지 크기", defaultValue = "100", minimum = "1", maximum = "1000")
    val size: Int = 100,
)
