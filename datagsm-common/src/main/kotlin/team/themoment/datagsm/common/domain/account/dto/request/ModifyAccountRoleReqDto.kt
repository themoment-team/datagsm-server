package team.themoment.datagsm.common.domain.account.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole

data class ModifyAccountRoleReqDto(
    @field:NotNull(message = "변경할 역할은 필수입니다.")
    @param:Schema(description = "변경할 계정 역할 (ADMIN, USER)")
    val role: AccountRole,
)
