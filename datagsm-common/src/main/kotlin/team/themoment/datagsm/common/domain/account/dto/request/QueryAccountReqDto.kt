package team.themoment.datagsm.common.domain.account.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.entity.constant.AccountSortBy
import team.themoment.datagsm.common.global.constant.SortDirection

data class QueryAccountReqDto(
    @param:Schema(description = "이메일 (부분 일치)")
    val email: String? = null,
    @param:Schema(description = "계정 역할 (ROOT, ADMIN, USER)")
    val role: AccountRole? = null,
    @param:Schema(description = "학생 연결 여부 (true: 연결 계정만, false: 미연결 계정만)")
    val isStudent: Boolean? = null,
    @field:Min(0)
    @param:Schema(description = "페이지 번호", defaultValue = "0", minimum = "0")
    val page: Int = 0,
    @field:Min(1)
    @field:Max(1000)
    @param:Schema(description = "페이지 크기", defaultValue = "300", minimum = "1", maximum = "1000")
    val size: Int = 300,
    @param:Schema(description = "정렬 기준 (ID, EMAIL, ROLE, CREATED_AT)")
    val sortBy: AccountSortBy? = null,
    @param:Schema(description = "정렬 방향 (ASC, DESC)", defaultValue = "ASC")
    val sortDirection: SortDirection = SortDirection.ASC,
)
