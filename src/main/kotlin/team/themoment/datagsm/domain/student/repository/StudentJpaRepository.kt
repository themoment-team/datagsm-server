package team.themoment.datagsm.domain.student.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.repository.custom.StudentJpaCustomRepository
import java.util.Optional

interface StudentJpaRepository :
    JpaRepository<StudentJpaEntity, Long>,
    StudentJpaCustomRepository {
    fun findByEmail(email: String): Optional<StudentJpaEntity>

    @Query(
        """
    SELECT t FROM StudentJpaEntity t
    LEFT JOIN FETCH t.majorClub
    LEFT JOIN FETCH t.jobClub
    LEFT JOIN FETCH t.autonomousClub
    WHERE t.studentNumber.studentGrade = :grade
    ORDER BY (
        t.studentNumber.studentGrade * 1000 +
        t.studentNumber.studentClass * 100 +
        t.studentNumber.studentNumber
    ) ASC
""",
    )
    fun findStudentsByGrade(grade: Int): List<StudentJpaEntity>

    @Query(
        """
        SELECT s FROM StudentJpaEntity s
        WHERE (s.studentNumber.studentGrade * 1000 +
        s.studentNumber.studentClass * 100 +
        s.studentNumber.studentNumber) IN :studentNumbers
    """,
    )
    fun findAllByStudentNumberIn(
        @Param("studentNumbers") studentNumbers: List<Int>,
    ): List<StudentJpaEntity>

    fun findByMajorClub(club: ClubJpaEntity): List<StudentJpaEntity>

    fun findByJobClub(club: ClubJpaEntity): List<StudentJpaEntity>

    fun findByAutonomousClub(club: ClubJpaEntity): List<StudentJpaEntity>
}
