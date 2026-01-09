package team.themoment.datagsm.common.domain.student.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class StudentNumber {
    @field:Column(name = "student_grade", nullable = false)
    var studentGrade: Int = 0

    @field:Column(name = "student_class", nullable = false)
    var studentClass: Int = 0

    @field:Column(name = "student_number", nullable = false)
    var studentNumber: Int = 0

    constructor()

    constructor(grade: Int, classNum: Int, number: Int) {
        this.studentGrade = grade
        this.studentClass = classNum
        this.studentNumber = number
    }

    val fullStudentNumber: Int
        get() = studentGrade * 1000 + studentClass * 100 + studentNumber
}
