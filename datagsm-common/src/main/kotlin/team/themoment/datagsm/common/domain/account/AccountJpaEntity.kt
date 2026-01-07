package team.themoment.datagsm.common.domain.account

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.UpdateTimestamp
import team.themoment.datagsm.common.domain.account.AccountRole
import team.themoment.datagsm.common.domain.student.StudentJpaEntity
import java.time.LocalDateTime

@Table(name = "tb_account")
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

    @OneToOne
    @JoinColumn(name = "student_id", nullable = true, referencedColumnName = "id")
    var student: StudentJpaEntity? = null

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
