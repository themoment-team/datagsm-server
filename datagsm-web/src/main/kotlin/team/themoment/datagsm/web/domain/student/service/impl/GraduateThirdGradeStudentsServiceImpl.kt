package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.GraduateThirdGradeStudentsService

@Service
class GraduateThirdGradeStudentsServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
) : GraduateThirdGradeStudentsService {
    @Transactional
    override fun execute(): Int {
        val thirdGradeStudents = studentJpaRepository.findStudentsByGrade(3)

        thirdGradeStudents.forEach { student ->
            student.role = StudentRole.GRADUATE
            student.major = null
            student.studentNumber = null
            student.dormitoryRoomNumber = null
            student.majorClub = null
            student.jobClub = null
            student.autonomousClub = null
        }

        return thirdGradeStudents.size
    }
}
