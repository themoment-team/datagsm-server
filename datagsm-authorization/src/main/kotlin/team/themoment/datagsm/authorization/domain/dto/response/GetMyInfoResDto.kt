package team.themoment.datagsm.authorization.domain.account.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.account.AccountRole

data class GetMyInfoResDto(
    @param:Schema(description = "계정 ID", example = "1")
    val id: Long,
    @param:Schema(description = "계정 이메일", example = "user@gsm.hs.kr")
    val email: String,
    @param:Schema(description = "계정 역할 (ADMIN, USER)", example = "USER")
    val role: AccountRole,
)
