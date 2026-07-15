package team.themoment.datagsm.common.domain.teacher.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.teacher.entity.TeacherJpaEntity
import team.themoment.datagsm.common.domain.teacher.entity.constant.TeacherDepartment

data class TeacherResDto(
    @field:Schema(description = "선생님 ID", example = "1")
    val id: Long,
    @field:Schema(description = "이름", example = "김선생")
    val name: String,
    @field:Schema(description = "이메일", example = "teacher@gsm.hs.kr")
    val email: String,
    @field:Schema(description = "소속 부서", example = "GRADE")
    val department: TeacherDepartment,
    @field:Schema(description = "선생님 설명", example = "3학년 1반 담임선생님")
    val description: String?,
) {
    companion object {
        fun from(teacher: TeacherJpaEntity): TeacherResDto =
            TeacherResDto(
                id = teacher.id!!,
                name = teacher.name,
                email = teacher.email,
                department = teacher.department,
                description = teacher.description,
            )
    }
}
