package team.themoment.datagsm.domain.student.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.repository.custom.StudentJpaCustomRepository
import java.util.Optional

interface StudentJpaRepository :
    JpaRepository<StudentJpaEntity, Long>,
    StudentJpaCustomRepository {
    fun findByStudentEmail(email: String): Optional<StudentJpaEntity>
}
