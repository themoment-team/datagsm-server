package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.student.dto.response.GraduateStudentResDto
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.GraduateThirdGradeStudentsService

@Service
class GraduateThirdGradeStudentsServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
) : GraduateThirdGradeStudentsService {
    @Transactional
    override fun execute(): GraduateStudentResDto {
        val thirdGradeStudents = studentJpaRepository.findStudentsByGrade(3)
        val studentIds = thirdGradeStudents.map { it.id!! }

        val updatedCount = studentJpaRepository.graduateStudentsByIds(studentIds)

        return GraduateStudentResDto(graduatedCount = updatedCount)
    }
}
