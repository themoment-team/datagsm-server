package team.themoment.datagsm.domain.client.entity

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.global.common.converter.StringListConverter

@Table(name = "tb_client")
@Entity
class ClientJpaEntity {
    @Id
    lateinit var id: String
    lateinit var secret: String

    @Convert(converter = StringListConverter::class)
    lateinit var redirectUrl: List<String>

    lateinit var name: String

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    var account: AccountJpaEntity? = null
}
