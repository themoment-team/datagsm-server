package team.themoment.datagsm.common.domain.account.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class VerifyPasswordResetCodeReqDto(
    @field:NotBlank
    @field:Email
    @field:Size(max = 50)
    @param:Schema(
        description = "비밀번호를 재설정할 계정 이메일",
        example = "user@gsm.hs.kr",
        maxLength = 50,
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val email: String,
    @field:NotBlank
    @param:Schema(description = "이메일로 받은 비밀번호 재설정 인증 코드", example = "12345678", requiredMode = Schema.RequiredMode.REQUIRED)
    val code: String,
)
