package team.themoment.datagsm.common.domain.account.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class DeleteMyAccountReqDto(
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @param:Schema(description = "현재 비밀번호")
    val password: String,
)
