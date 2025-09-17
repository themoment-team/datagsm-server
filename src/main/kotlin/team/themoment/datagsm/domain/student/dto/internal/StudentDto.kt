package team.themoment.datagsm.domain.student.dto.internal

import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.Sex

data class StudentDto(
    val studentId: Long,
    val name: String,
    val sex: Sex,
    val email: String,
    val grade: Int,
    val classNum: Int,
    val number: Int,
    val major: Major,
    val role: Role,
    val dormitoryFloor: Int,
    val dormitoryRoom: Int,
    val isLeaveSchool: Boolean,
)
