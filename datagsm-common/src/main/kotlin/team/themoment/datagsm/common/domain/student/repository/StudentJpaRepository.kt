package team.themoment.datagsm.common.domain.student.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.student.entity.BaseStudent
import team.themoment.datagsm.common.domain.student.entity.EnrolledStudent
import team.themoment.datagsm.common.domain.student.repository.custom.StudentJpaCustomRepository
import java.util.Optional

interface StudentJpaRepository :
    JpaRepository<BaseStudent, Long>,
    StudentJpaCustomRepository {
    fun findByEmail(email: String): Optional<BaseStudent>

    fun findByMajorClub(club: ClubJpaEntity): List<EnrolledStudent>

    fun findByJobClub(club: ClubJpaEntity): List<EnrolledStudent>

    fun findByAutonomousClub(club: ClubJpaEntity): List<EnrolledStudent>

    fun findByStudentNumberStudentGradeAndStudentNumberStudentClassAndStudentNumberStudentNumberAndName(
        grade: Int,
        classNum: Int,
        number: Int,
        name: String,
    ): EnrolledStudent?
}
