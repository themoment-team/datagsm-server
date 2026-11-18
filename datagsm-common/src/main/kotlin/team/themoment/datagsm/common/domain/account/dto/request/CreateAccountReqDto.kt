package team.themoment.datagsm.common.domain.account.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.teacher.entity.constant.TeacherDepartment

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
    @param:Schema(description = "가입 대상 종류 (STUDENT는 ACTIVE, TEACHER는 승인 대기 PENDING으로 생성)", example = "STUDENT", defaultValue = "STUDENT")
    val objectType: AccountObjectType = AccountObjectType.STUDENT,
    @field:Size(max = 10)
    @param:Schema(
        description = "이름 (선생님 가입 시 필수, 학생 가입 시 무시)",
        example = "김선생",
        maxLength = 10,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val name: String? = null,
    @param:Schema(
        description = "소속 부서 (선생님 가입 시 필수, 학생 가입 시 무시)",
        example = "GRADE",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val department: TeacherDepartment? = null,
    @field:Size(max = 100)
    @param:Schema(
        description = "선생님 설명 (선생님 가입 시 선택, 학생 가입 시 무시)",
        example = "3학년 1반 담임선생님",
        maxLength = 100,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val description: String? = null,
)
