package team.themoment.datagsm.common.domain.student.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
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

    @Modifying
    @Query(
        value = """
            UPDATE tb_student
            SET student_type = 'NON_ENROLLED',
                role = 'GRADUATE',
                student_grade = NULL,
                student_class = NULL,
                student_number = NULL,
                major = NULL,
                major_club_id = NULL,
                job_club_id = NULL,
                autonomous_club_id = NULL,
                room_number = NULL
            WHERE id = :studentId
            AND student_type = 'ENROLLED'
        """,
        nativeQuery = true,
    )
    fun graduateStudentById(studentId: Long): Int

    @Modifying
    @Query(
        value = """
            UPDATE tb_student
            SET student_type = 'NON_ENROLLED',
                role = 'GRADUATE',
                student_grade = NULL,
                student_class = NULL,
                student_number = NULL,
                major = NULL,
                major_club_id = NULL,
                job_club_id = NULL,
                autonomous_club_id = NULL,
                room_number = NULL
            WHERE id IN (:studentIds)
            AND student_type = 'ENROLLED'
        """,
        nativeQuery = true,
    )
    fun graduateStudentsByIds(studentIds: List<Long>): Int

    @Modifying
    @Query(
        value = """
            UPDATE tb_student
            SET student_type = 'NON_ENROLLED',
                role = 'DROPOUT',
                student_grade = NULL,
                student_class = NULL,
                student_number = NULL,
                major = NULL,
                major_club_id = NULL,
                job_club_id = NULL,
                autonomous_club_id = NULL,
                room_number = NULL
            WHERE id = :studentId
            AND student_type = 'ENROLLED'
        """,
        nativeQuery = true,
    )
    fun dropoutStudentById(studentId: Long): Int
}
