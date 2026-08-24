package team.themoment.datagsm.common.domain.student.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentDataEditRequestJpaEntity
import java.util.Optional

interface StudentDataEditRequestJpaRepository : JpaRepository<StudentDataEditRequestJpaEntity, Long> {
    fun findByStudentId(studentId: Long): Optional<StudentDataEditRequestJpaEntity>

    fun findAllByStudentIdIn(studentIds: List<Long>): List<StudentDataEditRequestJpaEntity>
}
