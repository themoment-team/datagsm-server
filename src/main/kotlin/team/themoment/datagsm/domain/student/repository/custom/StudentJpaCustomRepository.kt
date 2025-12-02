package team.themoment.datagsm.domain.student.repository.custom

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentRole

interface StudentJpaCustomRepository {
    fun searchStudentsWithPaging(
        id: Long?,
        name: String?,
        email: String?,
        grade: Int?,
        classNum: Int?,
        number: Int?,
        sex: Sex?,
        role: StudentRole?,
        dormitoryRoom: Int?,
        isLeaveSchool: Boolean,
        pageable: Pageable,
    ): Page<StudentJpaEntity>

    fun existsByEmail(email: String): Boolean

    fun existsByStudentNumber(
        grade: Int,
        classNum: Int,
        number: Int,
    ): Boolean

    fun existsByStudentEmailAndNotId(
        email: String,
        id: Long,
    ): Boolean

    fun existsByStudentNumberAndNotId(
        grade: Int,
        classNum: Int,
        number: Int,
        id: Long,
    ): Boolean
}
