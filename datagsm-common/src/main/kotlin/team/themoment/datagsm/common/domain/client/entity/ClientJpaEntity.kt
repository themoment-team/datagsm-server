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
    var scopes: MutableSet<String> = mutableSetOf()

    @Column(name = "client_name")
    lateinit var clientName: String

    @Column(name = "service_name")
    lateinit var serviceName: String

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    lateinit var account: AccountJpaEntity
}
