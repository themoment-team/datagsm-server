package team.themoment.datagsm.domain.student.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.StudentNumber
import team.themoment.datagsm.domain.student.repository.custom.StudentJpaCustomRepository
import java.util.Optional

interface StudentJpaRepository :
    JpaRepository<StudentJpaEntity, Long>,
    StudentJpaCustomRepository {
    fun findByStudentEmail(email: String): Optional<StudentJpaEntity>

    @Query(
        """
    select t from StudentJpaEntity t
    left join fetch t.majorClub
    left join fetch t.jobClub
    left join fetch t.autonomousClub
    where t.studentNumber.studentGrade = :grade
    order by (
        t.studentNumber.studentGrade * 1000 +
        t.studentNumber.studentClass * 100 +
        t.studentNumber.studentNumber
    ) ASC
"""
    )
    fun findStudentsByGrade(grade: Int): List<StudentJpaEntity>


    @Query(
        """
        SELECT s FROM StudentJpaEntity s
        WHERE (s.studentNumber.studentGrade * 1000 +
        s.studentNumber.studentClass * 100 +
        s.studentNumber.studentNumber) IN :studentNumbers
    """
    )
    fun findAllByStudentNumberIn(
        @Param("studentNumbers") studentNumbers: List<Int>,
    ): List<StudentJpaEntity>
}
