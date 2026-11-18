package team.themoment.datagsm.common.domain.student.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole

data class UpdateStudentReqDto(
    @field:Size(max = 30)
    @field:NotBlank
    @param:Schema(description = "이름", example = "홍길동", maxLength = 30, requiredMode = Schema.RequiredMode.REQUIRED)
    val name: String,
    @param:Schema(description = "성별", example = "WOMAN", requiredMode = Schema.RequiredMode.REQUIRED)
    val sex: Sex,
    @field:Size(max = 50)
    @field:Email
    @field:NotBlank
    @param:Schema(description = "이메일", example = "student@gsm.hs.kr", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    val email: String,
    @field:Min(value = 1)
    @field:Max(value = 3)
    @param:Schema(description = "학년 (1-3)", example = "1", minimum = "1", maximum = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    val grade: Int,
    @field:Min(value = 1)
    @field:Max(value = 4)
    @param:Schema(description = "반 (1-4)", example = "1", minimum = "1", maximum = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    val classNum: Int,
    @field:Min(value = 1)
    @field:Max(value = 18)
    @param:Schema(description = "번호 (1-18)", example = "1", minimum = "1", maximum = "18", requiredMode = Schema.RequiredMode.REQUIRED)
    val number: Int,
    @param:Schema(
        description = "재학생 역할 (GRADUATE/WITHDRAWN 변경 불가)",
        example = "GENERAL_STUDENT",
        allowableValues = ["GENERAL_STUDENT", "STUDENT_COUNCIL", "DORMITORY_MANAGER"],
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val role: StudentRole,
    @field:Min(value = 201)
    @field:Max(value = 518)
    @param:Schema(
        description = "기숙사 호실 (201-518)",
        example = "301",
        minimum = "201",
        maximum = "518",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val dormitoryRoomNumber: Int? = null,
    @param:Schema(description = "전공 동아리 ID", example = "1", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val majorClubId: Long? = null,
    @param:Schema(description = "자율 동아리 ID", example = "3", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val autonomousClubId: Long? = null,
    @field:Size(max = 50)
    @param:Schema(description = "전공", example = "백엔드", maxLength = 50, nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val specialty: String? = null,
    @field:Size(max = 39)
    @field:Pattern(regexp = "^[a-zA-Z0-9]([a-zA-Z0-9-]{0,37}[a-zA-Z0-9])?$", message = "유효하지 않은 GitHub 아이디 형식입니다.")
    @param:Schema(
        description = "GitHub 아이디 (null이면 초기화)",
        example = "torvalds",
        maxLength = 39,
        pattern = "^[a-zA-Z0-9]([a-zA-Z0-9-]{0,37}[a-zA-Z0-9])?$",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val githubId: String? = null,
)
