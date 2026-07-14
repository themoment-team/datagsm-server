package team.themoment.datagsm.common.domain.teacher.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicUpdate

@Table(name = "tb_teacher")
@Entity
@DynamicUpdate
class TeacherJpaEntity {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:Column(name = "id")
    var id: Long? = null

    @field:Column(name = "name", nullable = false, length = 10)
    lateinit var name: String

    @field:Column(name = "email", nullable = false, unique = true, length = 25)
    lateinit var email: String

    companion object {
        fun create(
            name: String,
            email: String,
        ): TeacherJpaEntity =
            TeacherJpaEntity().apply {
                this.name = name
                this.email = email
            }
    }
}
