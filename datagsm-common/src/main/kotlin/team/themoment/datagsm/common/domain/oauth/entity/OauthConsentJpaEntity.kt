package team.themoment.datagsm.common.domain.oauth.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * 사용자가 특정 클라이언트에 허용한 scope 기록.
 * SSO 세션으로 로그인을 생략할 때, 이미 동의한 scope인지 판단하는 근거가 된다.
 */
@Table(
    name = "tb_oauth_consent",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_oauth_consent_account_client", columnNames = ["account_id", "client_id"]),
    ],
)
@Entity
class OauthConsentJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null

    @Column(name = "account_id", nullable = false)
    var accountId: Long = 0

    @Column(name = "client_id", nullable = false)
    lateinit var clientId: String

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "tb_oauth_consent_scope",
        joinColumns = [JoinColumn(name = "consent_id")],
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Column(name = "scope")
    val grantedScopes: MutableSet<String> = mutableSetOf()

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null

    companion object {
        fun create(
            accountId: Long,
            clientId: String,
            scopes: Set<String>,
        ): OauthConsentJpaEntity =
            OauthConsentJpaEntity().apply {
                this.accountId = accountId
                this.clientId = clientId
                this.grantedScopes.addAll(scopes)
            }
    }
}
