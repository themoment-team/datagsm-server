package team.themoment.datagsm.common.domain.student.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.repository.custom.StudentJpaCustomRepository
import java.util.Optional

interface StudentJpaRepository :
    JpaRepository<StudentJpaEntity, Long>,
    StudentJpaCustomRepository {
    fun findByEmail(email: String): Optional<StudentJpaEntity>

    fun findByMajorClub(club: ClubJpaEntity): List<StudentJpaEntity>

    fun findByAutonomousClub(club: ClubJpaEntity): List<StudentJpaEntity>

    fun findByStudentNumberStudentGradeAndStudentNumberStudentClassAndStudentNumberStudentNumberAndName(
        grade: Int,
        classNum: Int,
        number: Int,
        name: String,
    ): StudentJpaEntity?

    @Query(
        "SELECT s FROM StudentJpaEntity s " +
            "WHERE (s.studentNumber.studentGrade * 1000 + s.studentNumber.studentClass * 100 + s.studentNumber.studentNumber) IN :codes",
    )
    fun findAllByStudentNumberCodes(@Param("codes") codes: List<Int>): List<StudentJpaEntity>
}
