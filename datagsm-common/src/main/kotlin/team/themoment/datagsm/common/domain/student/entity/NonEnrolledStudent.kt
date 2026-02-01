package team.themoment.datagsm.common.domain.student.entity

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("NON_ENROLLED")
class NonEnrolledStudent : BaseStudent()
