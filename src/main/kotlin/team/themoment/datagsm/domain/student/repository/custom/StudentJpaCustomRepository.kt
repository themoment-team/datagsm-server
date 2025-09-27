package team.themoment.datagsm.domain.student.repository.custom

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.Sex

interface StudentJpaCustomRepository {
    fun searchStudentsWithPaging(
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
        pageable: Pageable,
    ): Page<StudentJpaEntity>

    fun existsByStudentEmail(email: String): Boolean

    fun existsByStudentNumber(
        grade: Int,
        classNum: Int,
        number: Int,
    ): Boolean

    fun existsByStudentEmailAndNotStudentId(
        email: String,
        studentId: Long,
    ): Boolean

    fun existsByStudentNumberAndNotStudentId(
        grade: Int,
        classNum: Int,
        number: Int,
        studentId: Long,
    ): Boolean
}
