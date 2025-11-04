package team.themoment.datagsm.domain.club.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicUpdate
import team.themoment.datagsm.domain.club.entity.constant.ClubType

@Table(name = "tb_club")
@Entity
@DynamicUpdate
class ClubJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    var clubId: Long? = null

    @Column(name = "club_name", nullable = false, unique = true, length = 50)
    lateinit var clubName: String

    @Column(name = "club_type", nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var clubType: ClubType
}
