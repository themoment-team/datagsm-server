package team.themoment.datagsm.resource.domain.student.service

import team.themoment.datagsm.common.domain.student.Sex
import team.themoment.datagsm.common.domain.student.StudentRole
import team.themoment.datagsm.common.domain.student.StudentSortBy
import team.themoment.datagsm.common.dto.student.response.StudentListResDto
import team.themoment.datagsm.common.global.constant.SortDirection

interface QueryStudentService {
    fun execute(
        studentId: Long?,
        name: String?,
        email: String?,
        grade: Int?,
        classNum: Int?,
        number: Int?,
        sex: Sex?,
        role: StudentRole?,
        dormitoryRoom: Int?,
        isLeaveSchool: Boolean?,
        page: Int,
        size: Int,
        sortBy: StudentSortBy? = null,
        sortDirection: SortDirection = SortDirection.ASC,
    ): StudentListResDto
}
