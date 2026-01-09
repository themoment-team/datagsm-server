package team.themoment.datagsm.common.domain.account.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto

data class GetMyInfoResDto(
    @param:Schema(description = "계정 ID", example = "1")
    val id: Long,
    @param:Schema(description = "계정 이메일", example = "user@gsm.hs.kr")
    val email: String,
    @param:Schema(description = "계정 역할 (ADMIN, USER)", example = "USER")
    val role: AccountRole,
    @param:Schema(description = "학생 계정 여부", example = "true")
    val isStudent: Boolean,
    @param:Schema(description = "학생 정보 (학생인 경우에만 포함)")
    val student: StudentResDto?,
)
