package team.themoment.datagsm.common.domain.account.dto.internal

import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.common.domain.teacher.dto.response.TeacherResDto

/**
 * 계정에 연결된 대상(학생/선생님)을 조회한 결과를 담는 내부 DTO입니다.
 */
data class ResolvedAccountObject(
    val student: StudentResDto?,
    val teacher: TeacherResDto?,
)
