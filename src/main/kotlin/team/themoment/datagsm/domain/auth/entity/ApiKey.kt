package team.themoment.datagsm.domain.auth.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicUpdate
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "tb_api_key_scope",
        joinColumns = [JoinColumn(name = "api_key_id")],
    )
    @Column(name = "scope")
    private val _scopes: MutableSet<String> = mutableSetOf()

    val scopes: Set<String>
        get() = _scopes.toSet()

    @Column(name = "description", length = 500)
    var description: String? = null

    @Column(name = "rate_limit_capacity", nullable = false)
    var rateLimitCapacity: Long = 100

    @Column(name = "rate_limit_refill_tokens", nullable = false)
    var rateLimitRefillTokens: Long = 100

    @Column(name = "rate_limit_refill_duration_seconds", nullable = false)
    var rateLimitRefillDurationSeconds: Long = 60

    fun updateScopes(newScopes: Set<String>) {
        _scopes.clear()
        _scopes.addAll(newScopes)
    }

    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)

    fun canBeRenewed(renewalPeriodDays: Long): Boolean {
        val now = LocalDateTime.now()
        val renewalEndDate = expiresAt.plusDays(renewalPeriodDays)
        return now.isBefore(renewalEndDate)
    }

    fun getMaskedValue(): String {
        val uuidString = value.toString()
        return "${uuidString.take(8)}-****-****-****-********${uuidString.takeLast(4)}"
    }
}
