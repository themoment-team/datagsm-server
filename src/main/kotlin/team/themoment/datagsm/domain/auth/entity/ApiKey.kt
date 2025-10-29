package team.themoment.datagsm.domain.auth.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicUpdate
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import java.time.LocalDateTime
import java.util.UUID

@Table(name = "tb_api_key")
@Entity
@DynamicUpdate
class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "api_key_id")
    var apiKeyId: Long? = null

    @Column(name = "api_key_value", nullable = false, unique = true)
    var apiKeyValue: UUID = UUID.randomUUID()

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime = LocalDateTime.now()

    @OneToOne
    @JoinColumn(name = "api_key_student_id", nullable = false, referencedColumnName = "student_id")
    var apiKeyStudent: StudentJpaEntity? = null

    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)

    fun canBeRenewed(renewalPeriodDays: Long): Boolean {
        val now = LocalDateTime.now()
        val renewalStartDate = expiresAt.minusDays(renewalPeriodDays)
        val renewalEndDate = expiresAt.plusDays(renewalPeriodDays)
        return !now.isBefore(renewalStartDate) && now.isBefore(renewalEndDate)
    }
}
