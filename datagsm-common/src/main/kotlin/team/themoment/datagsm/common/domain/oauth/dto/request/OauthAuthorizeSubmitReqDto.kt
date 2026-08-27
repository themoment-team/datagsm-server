package team.themoment.datagsm.common.domain.oauth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class OauthAuthorizeSubmitReqDto(
    @param:Schema(description = "이메일", example = "user@gsm.hs.kr")
    @field:Email(message = "유효한 이메일 형식이어야 합니다.")
    @field:NotBlank(message = "이메일은 필수입니다.")
    val email: String,
    @param:Schema(description = "비밀번호", example = "password123!", minLength = 8, maxLength = 100)
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
    val password: String,
    @param:Schema(description = "인증 상태 토큰", example = "abc123-def456-...")
    @field:NotBlank(message = "인증 상태 토큰은 필수입니다.")
    val token: String,
    @field:Min(value = 1)
    @field:Max(value = 3)
    @param:Schema(description = "학년 (1-3, 정보 수정 요청 해소용)", example = "1", minimum = "1", maximum = "3")
    val studentGrade: Int? = null,
    @field:Min(value = 1)
    @field:Max(value = 4)
    @param:Schema(description = "반 (1-4, 정보 수정 요청 해소용)", example = "1", minimum = "1", maximum = "4")
    val studentClass: Int? = null,
    @field:Min(value = 1)
    @field:Max(value = 18)
    @param:Schema(description = "번호 (1-18, 정보 수정 요청 해소용)", example = "1", minimum = "1", maximum = "18")
    val studentNumber: Int? = null,
    @field:Min(value = 201)
    @field:Max(value = 518)
    @param:Schema(description = "기숙사 호실 (201-518, 정보 수정 요청 해소용)", example = "301", minimum = "201", maximum = "518")
    val dormitoryRoomNumber: Int? = null,
    @param:Schema(description = "전공 동아리 ID (정보 수정 요청 해소용)", example = "1")
    val majorClubId: Long? = null,
    @param:Schema(description = "자율 동아리 ID (정보 수정 요청 해소용)", example = "3")
    val autonomousClubId: Long? = null,
)
