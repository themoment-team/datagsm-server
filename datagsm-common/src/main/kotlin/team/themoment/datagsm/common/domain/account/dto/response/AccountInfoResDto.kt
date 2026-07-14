package team.themoment.datagsm.common.domain.account.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.common.domain.teacher.dto.response.TeacherResDto

data class AccountInfoResDto(
    @field:Schema(description = "계정 ID", example = "1")
    val id: Long,
    @field:Schema(description = "계정 이메일", example = "user@gsm.hs.kr")
    val email: String,
    @field:Schema(description = "계정 역할 (ADMIN, USER)", example = "USER")
    val role: AccountRole,
    @field:Schema(description = "계정 상태 (PENDING, ACTIVE)", example = "ACTIVE")
    val status: AccountStatus,
    @field:Schema(description = "연결 대상 종류 (STUDENT, TEACHER)", example = "STUDENT")
    val objectType: AccountObjectType?,
    @field:Schema(description = "학생 정보 (학생인 경우에만 포함)")
    val student: StudentResDto?,
    @field:Schema(description = "선생님 정보 (선생님인 경우에만 포함)")
    val teacher: TeacherResDto?,
)
