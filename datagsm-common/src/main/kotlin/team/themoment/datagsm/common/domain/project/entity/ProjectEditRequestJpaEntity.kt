package team.themoment.datagsm.common.domain.project.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicUpdate
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import java.time.LocalDateTime

@Table(
    name = "tb_project_edit_request",
    indexes = [
        Index(name = "idx_project_edit_request_original_project_id", columnList = "original_project_id"),
        Index(name = "idx_project_edit_request_requested_by_id", columnList = "requested_by_id"),
        Index(name = "idx_project_edit_request_status", columnList = "request_status"),
    ],
)
@Entity
@DynamicUpdate
class ProjectEditRequestJpaEntity {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:Column(name = "id")
    var id: Long? = null

    /** null이면 신규 프로젝트 생성 신청, 값이 있으면 해당 프로젝트에 대한 수정 신청 */
    @field:ManyToOne(optional = true)
    @field:JoinColumn(name = "original_project_id", nullable = true, referencedColumnName = "id")
    var originalProject: ProjectJpaEntity? = null

    @field:ManyToOne(optional = false)
    @field:JoinColumn(name = "requested_by_id", nullable = false, referencedColumnName = "id")
    lateinit var requestedBy: StudentJpaEntity

    @field:Column(name = "name", nullable = false, length = 100)
    lateinit var name: String

    @field:Column(name = "description", nullable = false, length = 500, columnDefinition = "TEXT")
    lateinit var description: String

    @field:Column(name = "start_year", nullable = false)
    var startYear: Int = 0

    @field:Column(name = "icon_key", nullable = true, length = 300)
    var iconKey: String? = null

    @field:ManyToOne(optional = true)
    @field:JoinColumn(name = "club_id", nullable = true, referencedColumnName = "id")
    var club: ClubJpaEntity? = null

    @field:ManyToMany
    @field:JoinTable(
        name = "tb_project_edit_request_participant",
        joinColumns = [JoinColumn(name = "project_edit_request_id")],
        inverseJoinColumns = [JoinColumn(name = "student_id")],
    )
    var participants: MutableSet<StudentJpaEntity> = mutableSetOf()

    @field:ElementCollection(fetch = FetchType.LAZY)
    @field:CollectionTable(
        name = "tb_project_edit_request_repository",
        joinColumns = [JoinColumn(name = "project_edit_request_id")],
    )
    @field:Column(name = "repository_url", nullable = false, length = 300)
    var repositories: MutableSet<String> = mutableSetOf()

    @field:ElementCollection(fetch = FetchType.LAZY)
    @field:CollectionTable(
        name = "tb_project_edit_request_tech_stack",
        joinColumns = [JoinColumn(name = "project_edit_request_id")],
    )
    @field:Column(name = "tech_stack_name", nullable = false, length = 50)
    var techStacks: MutableSet<String> = mutableSetOf()

    @field:Column(name = "request_status", nullable = false)
    @field:Enumerated(EnumType.STRING)
    var requestStatus: ProjectRequestStatus = ProjectRequestStatus.PENDING

    @field:Column(name = "reject_reason", nullable = true, length = 500)
    var rejectReason: String? = null

    @field:Column(name = "requested_at", nullable = false)
    var requestedAt: LocalDateTime = LocalDateTime.now()

    @field:Column(name = "processed_at", nullable = true)
    var processedAt: LocalDateTime? = null
}
