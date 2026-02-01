package team.themoment.datagsm.common.domain.student.entity

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Major

@Entity
@DiscriminatorValue("ENROLLED")
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_student_number",
            columnNames = ["student_grade", "student_class", "student_number"],
        ),
    ],
)
class EnrolledStudent : BaseStudent() {
    @Embedded
    var studentNumber: StudentNumber = StudentNumber()

    @Column(name = "major", nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var major: Major

    @ManyToOne(optional = true)
    @JoinColumn(name = "major_club_id", nullable = true, referencedColumnName = "id")
    var majorClub: ClubJpaEntity? = null

    @ManyToOne(optional = true)
    @JoinColumn(name = "job_club_id", nullable = true, referencedColumnName = "id")
    var jobClub: ClubJpaEntity? = null

    @ManyToOne(optional = true)
    @JoinColumn(name = "autonomous_club_id", nullable = true, referencedColumnName = "id")
    var autonomousClub: ClubJpaEntity? = null

    @Embedded
    var dormitoryRoomNumber: DormitoryRoomNumber? = null
}
