package team.themoment.datagsm.domain.student.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.repository.custom.StudentJpaCustomRepository
import java.util.Optional

interface StudentJpaRepository :
    JpaRepository<StudentJpaEntity, Long>,
    StudentJpaCustomRepository {
    fun findByEmail(email: String): Optional<StudentJpaEntity>

    fun findByMajorClub(club: ClubJpaEntity): List<StudentJpaEntity>

    fun findByJobClub(club: ClubJpaEntity): List<StudentJpaEntity>

    fun findByAutonomousClub(club: ClubJpaEntity): List<StudentJpaEntity>
}
