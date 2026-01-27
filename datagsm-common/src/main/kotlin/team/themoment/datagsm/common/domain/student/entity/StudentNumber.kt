package team.themoment.datagsm.common.domain.student.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class StudentNumber {
    @field:Column(name = "student_grade", nullable = true)
    var studentGrade: Int? = null

    @field:Column(name = "student_class", nullable = true)
    var studentClass: Int? = null

    @field:Column(name = "student_number", nullable = true)
    var studentNumber: Int? = null

    constructor()

    constructor(grade: Int?, classNum: Int?, number: Int?) {
        this.studentGrade = grade
        this.studentClass = classNum
        this.studentNumber = number
    }

    val fullStudentNumber: Int?
        get() =
            if (studentGrade != null && studentClass != null && studentNumber != null) {
                studentGrade!! * 1000 + studentClass!! * 100 + studentNumber!!
            } else {
                null
            }
}
