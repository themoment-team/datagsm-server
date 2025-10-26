package team.themoment.datagsm.domain.student.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.student.entity.constant.Sex

data class StudentCreateReqDto(
    @param:Size(max = 30)
    @param:NotBlank
    @param:Schema(description = "이름", example = "홍길동", maxLength = 30)
    val name: String,
    @param:NotBlank
    @param:Schema(description = "성별", example = "MALE")
    val sex: Sex,
    @param:Size(max = 50)
    @param:Email
    @param:NotBlank
    @param:Schema(description = "이메일", example = "student@gsm.hs.kr", maxLength = 50)
    val email: String,
    @param:Min(value = 1)
    @param:Max(value = 3)
    @param:NotBlank
    @param:Schema(description = "학년 (1-3)", example = "1", minimum = "1", maximum = "3")
    val grade: Int,
    @param:Min(value = 1)
    @param:Max(value = 4)
    @param:NotBlank
    @param:Schema(description = "반 (1-4)", example = "1", minimum = "1", maximum = "4")
    val classNum: Int,
    @param:Min(value = 1)
    @param:Max(value = 18)
    @param:NotBlank
    @param:Schema(description = "번호 (1-18)", example = "1", minimum = "1", maximum = "18")
    val number: Int,
    @param:Schema(description = "역할", example = "GENERAL_STUDENT")
    val role: Role = Role.GENERAL_STUDENT,
    @param:Min(value = 201)
    @param:Max(value = 518)
    @param:Schema(description = "기숙사 호실 (201-518)", example = "301", minimum = "201", maximum = "518")
    val dormitoryRoomNumber: Int,
)
