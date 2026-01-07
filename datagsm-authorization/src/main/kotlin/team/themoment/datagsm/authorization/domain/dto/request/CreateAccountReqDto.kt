package team.themoment.datagsm.authorization.domain.account.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateAccountReqDto(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "유효한 이메일 형식이어야 합니다.")
    @field:Size(max = 50)
    @param:Schema(description = "계정 이메일", example = "user@gsm.hs.kr", maxLength = 50)
    val email: String,
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Size(min = 8, max = 100)
    @param:Schema(description = "계정 비밀번호", example = "password123!", minLength = 8, maxLength = 100)
    val password: String,
    @field:NotBlank(message = "인증 코드는 필수입니다.")
    @param:Schema(description = "이메일 인증 코드", example = "12345678")
    val code: String,
)
