package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.student.entity.EnrolledStudent
import team.themoment.datagsm.common.domain.student.entity.NonEnrolledStudent
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.GraduateStudentService
import team.themoment.sdk.exception.ExpectedException

@Service
class GraduateStudentServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
) : GraduateStudentService {
    @Transactional
    override fun execute(studentId: Long) {
        val student =
            studentJpaRepository.findById(studentId).orElseThrow {
                ExpectedException("학생을 찾을 수 없습니다. ID: $studentId", HttpStatus.NOT_FOUND)
            }

        if (student !is EnrolledStudent) {
            throw ExpectedException("이미 재학생이 아닙니다.", HttpStatus.BAD_REQUEST)
        }

        val nonEnrolledStudent =
            NonEnrolledStudent().apply {
                id = student.id
                name = student.name
                email = student.email
                sex = student.sex
                role = StudentRole.GRADUATE
                isLeaveSchool = student.isLeaveSchool
            }

        studentJpaRepository.delete(student)
        studentJpaRepository.save(nonEnrolledStudent)
    }
}
