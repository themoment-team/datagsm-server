package team.themoment.datagsm.common.domain.account.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChangePasswordReqDto(
    @field:NotBlank
    @field:Email
    @field:Size(max = 50)
    val email: String,
    @field:NotBlank
    val code: String,
    @field:NotBlank
    @field:Size(min = 8, max = 100)
    val newPassword: String,
)
