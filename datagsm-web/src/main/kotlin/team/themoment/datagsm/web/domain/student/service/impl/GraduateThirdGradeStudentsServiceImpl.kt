package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.student.dto.response.GraduateStudentResDto
import team.themoment.datagsm.common.domain.student.entity.NonEnrolledStudent
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.GraduateThirdGradeStudentsService

@Service
class GraduateThirdGradeStudentsServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
) : GraduateThirdGradeStudentsService {
    @Transactional
    override fun execute(): GraduateStudentResDto {
        val thirdGradeStudents = studentJpaRepository.findStudentsByGrade(3)

        val nonEnrolledStudents =
            thirdGradeStudents.map { student ->
                NonEnrolledStudent().apply {
                    id = student.id
                    name = student.name
                    email = student.email
                    sex = student.sex
                    role = StudentRole.GRADUATE
                    isLeaveSchool = student.isLeaveSchool
                }
            }

        studentJpaRepository.deleteAll(thirdGradeStudents)
        studentJpaRepository.saveAll(nonEnrolledStudents)

        return GraduateStudentResDto(graduatedCount = thirdGradeStudents.size)
    }
}
