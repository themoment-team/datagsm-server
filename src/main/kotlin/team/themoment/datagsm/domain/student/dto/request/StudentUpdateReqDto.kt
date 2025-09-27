package team.themoment.datagsm.domain.student.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.student.entity.constant.Sex

data class StudentUpdateReqDto(
    @param:Size(max = 30)
    val name: String?,
    val sex: Sex?,
    @param:Size(max = 50)
    @param:Email
    val email: String?,
    @param:Min(value = 1)
    @param:Max(value = 3)
    val grade: Int?,
    @param:Min(value = 1)
    @param:Max(value = 4)
    val classNum: Int?,
    @param:Min(value = 1)
    @param:Max(value = 18)
    val number: Int?,
    val role: Role?,
    @param:Min(value = 201)
    @param:Max(value = 518)
    val dormitoryRoomNumber: Int?,
)
