package team.themoment.datagsm.common.domain.application.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Table(name = "tb_thirdparty_scope")
@Entity
class ThirdPartyScopeJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "scope_name", nullable = false)
    lateinit var scopeName: String

    @Column(name = "description", nullable = false)
    lateinit var description: String

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    lateinit var application: ApplicationJpaEntity
}
