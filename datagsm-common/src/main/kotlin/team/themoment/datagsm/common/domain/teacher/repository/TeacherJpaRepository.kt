package team.themoment.datagsm.common.domain.teacher.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.common.domain.teacher.entity.TeacherJpaEntity
import java.util.Optional

interface TeacherJpaRepository : JpaRepository<TeacherJpaEntity, Long> {
    fun findByEmail(email: String): Optional<TeacherJpaEntity>
}
