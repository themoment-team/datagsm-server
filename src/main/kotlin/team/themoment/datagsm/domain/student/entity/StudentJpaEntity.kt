package team.themoment.datagsm.domain.student.entity

import jakarta.persistence.Column
import jakarta.persistence.Embedded
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
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.DormitoryRoomNumber
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentNumber
import team.themoment.datagsm.domain.student.entity.constant.StudentRole

@Table(name = "tb_student")
@Entity
@DynamicUpdate
class StudentJpaEntity {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:Column(name = "id")
    var id: Long? = null

    @field:Column(name = "name", nullable = false, length = 10)
    lateinit var name: String

    @field:Embedded
    lateinit var studentNumber: StudentNumber

    @field:Column(name = "email", nullable = false, unique = true, length = 25)
    lateinit var email: String

    @field:Column(name = "major", nullable = false)
    @field:Enumerated(EnumType.STRING)
    lateinit var major: Major

    @field:JoinColumn(name = "major_club_id", nullable = true, referencedColumnName = "id")
    @field:ManyToOne(optional = true)
    var majorClub: ClubJpaEntity? = null

    @field:ManyToOne(optional = true)
    @field:JoinColumn(name = "job_club_id", nullable = true, referencedColumnName = "id")
    var jobClub: ClubJpaEntity? = null

    @field:JoinColumn(name = "autonomous_club_id", nullable = true, referencedColumnName = "id")
    @field:ManyToOne(optional = true)
    var autonomousClub: ClubJpaEntity? = null

    @field:Embedded
    var dormitoryRoomNumber: DormitoryRoomNumber? = null

    @field:Column(name = "role", nullable = false)
    @field:Enumerated(EnumType.STRING)
    var role: StudentRole = StudentRole.GENERAL_STUDENT

    @field:Column(name = "is_leave_school", nullable = false)
    var isLeaveSchool: Boolean = false

    @field:Column(name = "sex", nullable = false)
    @field:Enumerated(EnumType.STRING)
    lateinit var sex: Sex
}
