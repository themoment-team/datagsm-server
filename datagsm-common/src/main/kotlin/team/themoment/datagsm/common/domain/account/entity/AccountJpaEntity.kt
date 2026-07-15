package team.themoment.datagsm.common.domain.account.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.UpdateTimestamp
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import java.time.LocalDateTime

@Table(
    name = "tb_account",
    indexes = [
        Index(name = "idx_account_object", columnList = "object_id, object_type"),
    ],
)
@Entity
@DynamicUpdate
class AccountJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null

    @Column(name = "email", nullable = false, unique = true)
    lateinit var email: String

    @Column(name = "password", nullable = false)
    lateinit var password: String

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    var role: AccountRole = AccountRole.USER

    @Column(name = "object_id", nullable = true)
    var objectId: Long? = null

    @Column(name = "object_type", nullable = true)
    @Enumerated(EnumType.STRING)
    var objectType: AccountObjectType? = null

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: AccountStatus = AccountStatus.ACTIVE

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null

    companion object {
        fun create(email: String): AccountJpaEntity =
            AccountJpaEntity().apply {
                this.email = email
            }
    }
}
