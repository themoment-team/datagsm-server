package team.themoment.datagsm.web.domain.student.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.common.domain.club.ClubJpaEntity
import team.themoment.datagsm.common.domain.student.StudentJpaEntity
import team.themoment.datagsm.web.domain.student.repository.custom.StudentJpaCustomRepository
import java.util.Optional

interface StudentJpaRepository :
    JpaRepository<StudentJpaEntity, Long>,
    StudentJpaCustomRepository {
    fun findByEmail(email: String): Optional<StudentJpaEntity>

    fun findByMajorClub(club: ClubJpaEntity): List<StudentJpaEntity>

    fun findByJobClub(club: ClubJpaEntity): List<StudentJpaEntity>

    fun findByAutonomousClub(club: ClubJpaEntity): List<StudentJpaEntity>

    fun findByStudentNumberStudentGradeAndStudentNumberStudentClassAndStudentNumberStudentNumberAndName(
        grade: Int,
        classNum: Int,
        number: Int,
        name: String,
    ): StudentJpaEntity?
}
