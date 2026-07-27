package team.themoment.datagsm.common.domain.event.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.entity.constant.EventVerificationStatus
import java.time.LocalDateTime

@Table(name = "tb_event")
@Entity
@DynamicUpdate
class EventJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null

    @Column(name = "target_url", nullable = false, length = 2048)
    lateinit var targetUrl: String

    @Column(name = "secret", nullable = false, length = 64)
    lateinit var secret: String

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "tb_event_type",
        joinColumns = [JoinColumn(name = "event_id")],
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Enumerated(EnumType.STRING)
    @Column(name = "event", nullable = false)
    var events: MutableSet<EventType> = mutableSetOf()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, referencedColumnName = "id")
    lateinit var account: AccountJpaEntity

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    @Column(name = "verification_status", nullable = false)
    @Enumerated(EnumType.STRING)
    var verificationStatus: EventVerificationStatus = EventVerificationStatus.PENDING

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null
}
