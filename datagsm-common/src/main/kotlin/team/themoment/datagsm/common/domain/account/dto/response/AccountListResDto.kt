package team.themoment.datagsm.common.domain.account.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class AccountListResDto(
    @field:Schema(description = "전체 페이지 수", example = "1")
    val totalPages: Int,
    @field:Schema(description = "전체 계정 수", example = "100")
    val totalElements: Long,
    @field:Schema(description = "계정 목록")
    val accounts: List<AccountResDto>,
)
