package team.themoment.datagsm.domain.student.dto.request

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
    val name: String,
    @param:NotBlank
    val sex: Sex,
    @param:Size(max = 50)
    @param:Email
    @param:NotBlank
    val email: String,
    @param:Min(value = 1)
    @param:Max(value = 3)
    @param:NotBlank
    val grade: Int,
    @param:Min(value = 1)
    @param:Max(value = 4)
    @param:NotBlank
    val classNum: Int,
    @param:Min(value = 1)
    @param:Max(value = 18)
    @param:NotBlank
    val number: Int,
    val role: Role = Role.GENERAL_STUDENT,
    @param:Min(value = 201)
    @param:Max(value = 518)
    val dormitoryRoomNumber: Int,
)
