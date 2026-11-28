package team.themoment.datagsm.common.domain.client.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.common.global.converter.StringSetConverter

@Table(
    name = "tb_client",
    indexes = [
        Index(name = "idx_client_account_id", columnList = "account_id"),
        Index(name = "idx_client_client_name", columnList = "client_name"),
        Index(name = "idx_client_service_name", columnList = "service_name"),
    ],
)
@Entity
class ClientJpaEntity {
    @Id
    lateinit var id: String

    /**
     * PasswordEncoder로 암호화된 UUID 문자열 값
     */
    lateinit var secret: String

    @Convert(converter = StringSetConverter::class)
    @Column(columnDefinition = "text")
    lateinit var redirectUrls: Set<String>

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "tb_client_scope",
        joinColumns = [JoinColumn(name = "client_id")],
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Column(name = "scope")
    val scopes: MutableSet<String> = mutableSetOf()

    @Column(name = "client_name")
    lateinit var clientName: String

    @Column(name = "service_name")
    lateinit var serviceName: String

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    lateinit var account: AccountJpaEntity

    // 발급 당시의 scope 중 지금도 이 클라이언트에 남아 있는 것만 남긴다.
    // openid는 client scope에 저장되지 않는 OIDC 프로토콜 scope라 비교하지 않고 유지한다.
    fun retainCurrentScopes(grantedScopes: Set<String>): Set<String> =
        grantedScopes.filter { it == OAuthScope.OPENID || it in scopes }.toSet()
}
