package team.themoment.datagsm.domain.auth.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicUpdate
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import jakarta.persistence.OneToOne
import java.util.UUID

@Table(name = "tb_api_key")
@Entity
@DynamicUpdate
class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "api_key_id")
    var apiKeyId: Long? = null

    @Column(name = "api_key_value", nullable = false, unique = true)
    var apiKeyValue: UUID = UUID.randomUUID()

    @OneToOne
    @JoinColumn(name = "api_key_student_id", nullable = false, referencedColumnName = "student_id")
    var apiKeyStudent: StudentJpaEntity? = null
}
