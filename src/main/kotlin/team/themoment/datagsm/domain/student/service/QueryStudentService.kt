package team.themoment.datagsm.domain.student.service

import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.student.dto.response.StudentReqDto
import team.themoment.datagsm.domain.student.entity.constant.Sex

interface QueryStudentService {
    fun execute(
        studentId: Long?,
        name: String?,
        email: String?,
        grade: Int?,
        classNum: Int?,
        number: Int?,
        sex: Sex?,
        role: Role?,
        dormitoryRoom: Int?,
        isLeaveSchool: Boolean,
        page: Int,
        size: Int,
    ): StudentReqDto
}
