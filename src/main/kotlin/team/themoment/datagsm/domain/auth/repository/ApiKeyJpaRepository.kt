package team.themoment.datagsm.domain.auth.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import team.themoment.datagsm.domain.auth.entity.ApiKey
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import java.util.Optional
import java.util.UUID

@Repository
interface ApiKeyJpaRepository : JpaRepository<ApiKey, Long> {
    fun findByApiKeyStudent(student: StudentJpaEntity): Optional<ApiKey>

    fun findByApiKeyValue(apiKeyValue: UUID): Optional<ApiKey>

    fun deleteByApiKeyStudent(student: StudentJpaEntity)
}
