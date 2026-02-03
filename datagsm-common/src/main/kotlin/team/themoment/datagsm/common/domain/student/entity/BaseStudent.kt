package team.themoment.datagsm.common.domain.student.entity

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicUpdate
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole

@Entity
@Table(name = "tb_student")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "student_type", discriminatorType = DiscriminatorType.STRING)
@DynamicUpdate
abstract class BaseStudent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null

    @Column(name = "name", nullable = false, length = 10)
    lateinit var name: String

    @Column(name = "email", nullable = false, unique = true, length = 25)
    lateinit var email: String

    @Column(name = "sex", nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var sex: Sex

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    var role: StudentRole = StudentRole.GENERAL_STUDENT
}
