package team.themoment.datagsm.common.domain.student.repository.custom

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.student.entity.BaseStudent
import team.themoment.datagsm.common.domain.student.entity.EnrolledStudent
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.entity.constant.StudentSortBy
import team.themoment.datagsm.common.global.constant.SortDirection

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
        includeGraduates: Boolean = false,
        pageable: Pageable,
        sortBy: StudentSortBy?,
        sortDirection: SortDirection,
    ): Page<BaseStudent>

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

    fun findStudentsByGrade(grade: Int): List<EnrolledStudent>

    fun findAllStudents(): List<BaseStudent>

    fun searchRegisteredStudentsWithPaging(
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
        includeGraduates: Boolean = false,
        pageable: Pageable,
        sortBy: StudentSortBy?,
        sortDirection: SortDirection,
    ): Page<BaseStudent>

    fun findRegisteredStudentsByMajorClub(club: ClubJpaEntity): List<EnrolledStudent>

    fun findRegisteredStudentsByJobClub(club: ClubJpaEntity): List<EnrolledStudent>

    fun findRegisteredStudentsByAutonomousClub(club: ClubJpaEntity): List<EnrolledStudent>
}
