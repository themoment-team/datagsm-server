package team.themoment.datagsm.domain.student.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.repository.custom.StudentJpaCustomRepository

interface StudentJpaRepository :
    JpaRepository<StudentJpaEntity, Long>,
    StudentJpaCustomRepository
