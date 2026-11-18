package team.themoment.datagsm.common.domain.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectCategory
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectSortBy
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.global.constant.SortDirection

data class QueryProjectReqDto(
    @field:Positive
    @param:Schema(
        description = "프로젝트 ID",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        minimum = "1",
    )
    val projectId: Long? = null,
    @param:Schema(
        description = "프로젝트 이름 (앞부분 일치로 조회하고 요청 페이지의 결과가 없으면 부분 일치로 재조회)",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val projectName: String? = null,
    @field:Positive
    @param:Schema(
        description = "동아리 ID",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        minimum = "1",
    )
    val clubId: Long? = null,
    @param:Schema(
        description = "프로젝트 운영 상태 (ACTIVE, ENDED, 미입력 시 ACTIVE만 조회)",
        defaultValue = "ACTIVE",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val status: ProjectStatus? = ProjectStatus.ACTIVE,
    @param:Schema(
        description = "프로젝트 카테고리 (TOY_PROJECT, TEAM, CLUB, IDEA_FESTIVAL, NOT_DEPLOYED). 해당 카테고리를 포함하는 프로젝트만 조회하고 미입력 시 전체 조회",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val category: ProjectCategory? = null,
    @field:Min(0)
    @param:Schema(
        description = "페이지 번호",
        defaultValue = "0",
        minimum = "0",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val page: Int = 0,
    @field:Min(1)
    @field:Max(1000)
    @param:Schema(
        description = "페이지 크기",
        defaultValue = "100",
        minimum = "1",
        maximum = "1000",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val size: Int = 100,
    @param:Schema(
        description = "정렬 기준 (ID, NAME). 미입력 시 정렬하지 않으며 순서는 보장되지 않음",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val sortBy: ProjectSortBy? = null,
    @param:Schema(
        description = "정렬 방향 (ASC, DESC). sortBy가 없으면 무시됨",
        defaultValue = "ASC",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val sortDirection: SortDirection = SortDirection.ASC,
)
