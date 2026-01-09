package team.themoment.datagsm.common.domain.client.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.ApiScope
import team.themoment.datagsm.common.global.converter.StringSetConverter

@Table(name = "tb_client")
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

    @Convert(converter = StringSetConverter::class)
    @Column(columnDefinition = "text")
    lateinit var scopes: Set<ApiScope>

    lateinit var name: String

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    lateinit var account: AccountJpaEntity
}
