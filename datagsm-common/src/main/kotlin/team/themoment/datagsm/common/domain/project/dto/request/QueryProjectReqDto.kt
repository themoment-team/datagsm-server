package team.themoment.datagsm.common.domain.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectSortBy
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.global.constant.SortDirection

data class QueryProjectReqDto(
    @field:Positive
    @param:Schema(description = "프로젝트 ID")
    val projectId: Long? = null,
    @param:Schema(description = "프로젝트 이름")
    val projectName: String? = null,
    @field:Positive
    @param:Schema(description = "동아리 ID")
    val clubId: Long? = null,
    @param:Schema(description = "프로젝트 운영 상태 (ACTIVE, ENDED, 미입력 시 ACTIVE만 조회)", defaultValue = "ACTIVE")
    val status: ProjectStatus? = ProjectStatus.ACTIVE,
    @field:Min(0)
    @param:Schema(description = "페이지 번호", defaultValue = "0", minimum = "0")
    val page: Int = 0,
    @field:Min(1)
    @field:Max(1000)
    @param:Schema(description = "페이지 크기", defaultValue = "100", minimum = "1", maximum = "1000")
    val size: Int = 100,
    @param:Schema(description = "정렬 기준 (ID, NAME)")
    val sortBy: ProjectSortBy? = null,
    @param:Schema(description = "정렬 방향 (ASC, DESC)", defaultValue = "ASC")
    val sortDirection: SortDirection = SortDirection.ASC,
)
