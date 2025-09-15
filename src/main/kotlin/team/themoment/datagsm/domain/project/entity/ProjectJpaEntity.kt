package team.themoment.datagsm.domain.project.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicUpdate
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity

@Table(name = "tb_project")
@Entity
@DynamicUpdate
class ProjectJpaEntity {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:Column(name = "project_id")
    var projectId: Long? = null

    @field:Column(name = "project_name", nullable = false, length = 100)
    lateinit var projectName: String

    lateinit var discription: String

    @field:ManyToOne(optional = false)
    @field:JoinColumn(name = "owner_club_id", nullable = false, referencedColumnName = "club_id")
    lateinit var ownerClub: ClubJpaEntity
}
