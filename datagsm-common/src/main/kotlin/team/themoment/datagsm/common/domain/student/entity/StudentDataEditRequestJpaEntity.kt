package team.themoment.datagsm.common.domain.student.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.DynamicUpdate
import java.time.LocalDateTime

@Table(
    name = "tb_student_data_edit_request",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_student_data_edit_request_student_id", columnNames = ["student_id"]),
    ],
)
@Entity
@DynamicUpdate
class StudentDataEditRequestJpaEntity {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:Column(name = "id")
    var id: Long? = null

    @field:Column(name = "student_id", nullable = false, unique = true)
    var studentId: Long? = null

    @field:Column(name = "request_student_number", nullable = false)
    var requestStudentNumber: Boolean = false

    @field:Column(name = "request_dormitory_room_number", nullable = false)
    var requestDormitoryRoomNumber: Boolean = false

    @field:Column(name = "request_major_club", nullable = false)
    var requestMajorClub: Boolean = false

    @field:Column(name = "request_autonomous_club", nullable = false)
    var requestAutonomousClub: Boolean = false

    @field:CreationTimestamp
    @field:Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null
}
