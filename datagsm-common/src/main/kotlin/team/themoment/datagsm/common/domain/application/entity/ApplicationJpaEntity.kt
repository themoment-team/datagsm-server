package team.themoment.datagsm.common.domain.application.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity

@Table(name = "tb_application")
@Entity
class ApplicationJpaEntity {
    @Id
    @Column(name = "id")
    lateinit var id: String

    @Column(name = "name", nullable = false)
    lateinit var name: String

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    lateinit var account: AccountJpaEntity

    @OneToMany(mappedBy = "application", cascade = [CascadeType.ALL], orphanRemoval = true)
    var thirdPartyScopes: MutableList<ThirdPartyScopeJpaEntity> = mutableListOf()
}
