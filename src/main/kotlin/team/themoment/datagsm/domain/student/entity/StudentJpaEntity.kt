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
import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.DormitoryRoomNumber
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentNumber

@Table(name = "tb_student")
@Entity
@DynamicUpdate
class StudentJpaEntity {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:Column(name = "student_id")
    var studentId: Long? = null

    @field:Column(name = "student_name", nullable = false, length = 10)
    lateinit var studentName: String

    @field:Embedded
    lateinit var studentNumber: StudentNumber

    @field:Column(name = "student_email", nullable = false, unique = true, length = 25)
    lateinit var studentEmail: String

    @field:Column(name = "student_major", nullable = false)
    @field:Enumerated(EnumType.STRING)
    lateinit var studentMajor: Major

    @field:JoinColumn(name = "student_major_club_id", nullable = true, referencedColumnName = "club_id")
    @field:ManyToOne(optional = true)
    lateinit var studentMajorClub: ClubJpaEntity

    @field:ManyToOne(optional = true)
    @field:JoinColumn(name = "student_job_club_id", nullable = true, referencedColumnName = "club_id")
    lateinit var studentJobClub: ClubJpaEntity

    @field:JoinColumn(name = "student_autonomous_club_id", nullable = true, referencedColumnName = "club_id")
    @field:ManyToOne(optional = true)
    lateinit var studentAutonomousClub: ClubJpaEntity

    @field:Embedded
    lateinit var studentDormitoryRoomNumber: DormitoryRoomNumber

    @field:Column(name = "student_role", nullable = false)
    @field:Enumerated(EnumType.STRING)
    var studentRole: Role = Role.GENERAL_STUDENT

    @field:Column(name = "student_is_leave_school", nullable = false)
    var studentIsLeaveSchool: Boolean = false

    @field:Column(name = "student_sex", nullable = false)
    @field:Enumerated(EnumType.STRING)
    lateinit var studentSex: Sex
}
