package team.themoment.datagsm.common.domain.auth.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicUpdate
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.global.converter.StringSetConverter
import java.time.LocalDateTime
import java.util.UUID

@Table(name = "tb_api_key")
@Entity
@DynamicUpdate
class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null

    @Column(name = "value", nullable = false, unique = true)
    var value: UUID = UUID.randomUUID()

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime = LocalDateTime.now()

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false, referencedColumnName = "id")
    lateinit var account: AccountJpaEntity

    @Convert(converter = StringSetConverter::class)
    @Column(name = "scopes", columnDefinition = "json", nullable = false)
    var scopes: Set<String> = emptySet()

    @Column(name = "description", length = 500)
    var description: String? = null

    @Column(name = "rate_limit_capacity", nullable = false)
    var rateLimitCapacity: Long = 100

    @Column(name = "rate_limit_refill_tokens", nullable = false)
    var rateLimitRefillTokens: Long = 100

    @Column(name = "rate_limit_refill_duration_seconds", nullable = false)
    var rateLimitRefillDurationSeconds: Long = 60

    fun updateScopes(newScopes: Set<String>) {
        scopes = newScopes
    }

    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)

    fun canBeRenewed(renewalPeriodDays: Long): Boolean {
        val now = LocalDateTime.now()
        val renewalEndDate = expiresAt.plusDays(renewalPeriodDays)
        return now.isBefore(renewalEndDate)
    }

    val maskedValue: String
        get() {
            val uuidString = value.toString()
            return "${uuidString.take(8)}-****-****-****-********${uuidString.takeLast(4)}"
        }
}
