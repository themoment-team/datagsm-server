package team.themoment.datagsm.domain.account.dto.response

import team.themoment.datagsm.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.domain.student.dto.response.StudentResDto

data class GetMyInfoResDto(
    val id: Long,
    val email: String,
    val role: AccountRole,
    val isStudent: Boolean,
    val student: StudentResDto?,
)
