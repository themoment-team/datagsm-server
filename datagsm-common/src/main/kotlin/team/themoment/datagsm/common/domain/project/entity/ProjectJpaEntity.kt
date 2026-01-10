package team.themoment.datagsm.common.domain.project.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicUpdate
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity

@Table(name = "tb_project")
@Entity
@DynamicUpdate
class ProjectJpaEntity {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:Column(name = "id")
    var id: Long? = null

    @field:Column(name = "name", nullable = false, length = 100)
    lateinit var name: String

    @field:Column(name = "description", nullable = false, length = 500, columnDefinition = "TEXT")
    lateinit var description: String

    @field:ManyToOne(optional = true)
    @field:JoinColumn(name = "club_id", nullable = true, referencedColumnName = "id")
    var club: ClubJpaEntity? = null

    @field:ManyToMany
    @field:JoinTable(
        name = "tb_project_participant",
        joinColumns = [JoinColumn(name = "project_id")],
        inverseJoinColumns = [JoinColumn(name = "student_id")],
    )
    var participants: MutableSet<StudentJpaEntity> = mutableSetOf()
}
