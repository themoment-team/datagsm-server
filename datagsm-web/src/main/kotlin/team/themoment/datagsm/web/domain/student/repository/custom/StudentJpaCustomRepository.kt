package team.themoment.datagsm.web.domain.student.repository.custom

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.common.domain.student.Sex
import team.themoment.datagsm.common.domain.student.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.StudentRole
import team.themoment.datagsm.common.domain.student.StudentSortBy
import team.themoment.datagsm.web.global.common.constant.SortDirection

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
        isLeaveSchool: Boolean?,
        pageable: Pageable,
        sortBy: StudentSortBy?,
        sortDirection: SortDirection,
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

    fun findStudentsByGrade(grade: Int): List<StudentJpaEntity>

    fun findAllStudents(): List<StudentJpaEntity>
}
