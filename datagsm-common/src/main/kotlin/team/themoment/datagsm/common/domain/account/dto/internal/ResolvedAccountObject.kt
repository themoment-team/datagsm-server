package team.themoment.datagsm.common.domain.account.dto.internal

import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.project.dto.internal.ProjectSummaryDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.common.domain.teacher.dto.response.TeacherResDto

/**
 * 계정에 연결된 대상(학생/선생님)과 그 소속 동아리·참여 프로젝트를 조회한 결과를 담는 내부 DTO입니다.
 */
data class ResolvedAccountObject(
    val student: StudentResDto?,
    val teacher: TeacherResDto?,
    val clubs: List<ClubSummaryDto> = emptyList(),
    val projects: List<ProjectSummaryDto> = emptyList(),
)
