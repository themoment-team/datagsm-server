package team.themoment.datagsm.domain.client.entity

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.global.common.converter.StringListConverter

@Table(name = "tb_client")
@Entity
class ClientJpaEntity {
    @Id
    @GeneratedValue
    var id: Long? = null

    lateinit var clientId: String
    lateinit var clientSecret: String

    @Convert(converter = StringListConverter::class)
    lateinit var redirectUrl: List<String>

    lateinit var clientName: String

    @ManyToOne
    @JoinColumn(name = "owner_account_id", nullable = false)
    var owner: AccountJpaEntity? = null
}
