package team.themoment.datagsm.common.domain.account.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SendEmailReqDto(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "유효한 이메일 형식이어야 합니다.")
    @field:Size(max = 50)
    @param:Schema(description = "인증 코드를 받을 이메일", example = "user@gsm.hs.kr", maxLength = 50)
    val email: String,
)
