package team.themoment.datagsm.domain.account.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.UpdateTimestamp
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import java.time.LocalDateTime

@Table(name = "tb_account")
@Entity
@DynamicUpdate
class AccountJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    var accountId: Long? = null

    @Column(name = "account_email", nullable = false, unique = true)
    lateinit var accountEmail: String

    @OneToOne
    @JoinColumn(name = "account_student_id", nullable = true, referencedColumnName = "student_id")
    var accountStudent: StudentJpaEntity? = null

    @CreationTimestamp
    @Column(name = "account_created_at", nullable = false, updatable = false)
    var accountCreatedAt: LocalDateTime? = null

    @UpdateTimestamp
    @Column(name = "account_updated_at", nullable = false)
    var accountUpdatedAt: LocalDateTime? = null

    companion object {
        fun create(email: String): AccountJpaEntity =
            AccountJpaEntity().apply {
                this.accountEmail = email
            }
    }
}
