package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.dto.payload.StudentGraduatedData
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.service.EventPublisher
import team.themoment.datagsm.common.domain.student.dto.response.GraduateStudentResDto
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.GraduateThirdGradeStudentsService

@Service
class GraduateThirdGradeStudentsServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
    private val eventPublisher: EventPublisher,
) : GraduateThirdGradeStudentsService {
    @Transactional
    override fun execute(): GraduateStudentResDto {
        val thirdGradeStudents = studentJpaRepository.findStudentsByGrade(3)

        thirdGradeStudents.forEach { student ->
            student.role = StudentRole.GRADUATE
            student.major = null
            student.specialty = null
            student.studentNumber = null
            student.dormitoryRoomNumber = null
            student.majorClub = null
            student.autonomousClub = null
        }

        thirdGradeStudents.forEach { student ->
            eventPublisher.dispatch(
                EventType.STUDENT_GRADUATED,
                StudentGraduatedData(studentId = student.id!!, name = student.name, email = student.email),
            )
        }

        return GraduateStudentResDto(graduatedCount = thirdGradeStudents.size)
    }
}
