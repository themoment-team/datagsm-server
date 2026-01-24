package team.themoment.datagsm.common.domain.club.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicUpdate
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity

@Table(name = "tb_club")
@Entity
@DynamicUpdate
class ClubJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null

    @Column(name = "name", nullable = false, unique = true, length = 50)
    lateinit var name: String

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var type: ClubType

    @ManyToOne(optional = false)
    @JoinColumn(name = "leader_id", nullable = false, referencedColumnName = "id")
    lateinit var leader: StudentJpaEntity
}
