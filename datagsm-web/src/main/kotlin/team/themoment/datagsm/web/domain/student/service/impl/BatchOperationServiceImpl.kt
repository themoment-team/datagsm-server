package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.student.dto.request.BatchOperationReqDto
import team.themoment.datagsm.common.domain.student.dto.request.BatchOperationType
import team.themoment.datagsm.common.domain.student.dto.response.GraduateStudentResDto
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.BatchOperationService

@Service
class BatchOperationServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
) : BatchOperationService {
    @Transactional
    override fun execute(reqDto: BatchOperationReqDto): GraduateStudentResDto =
        when (reqDto.operation) {
            BatchOperationType.GRADUATE -> graduateStudents(reqDto.filter?.grade ?: 3)
        }

    private fun graduateStudents(grade: Int): GraduateStudentResDto {
        val students = studentJpaRepository.findStudentsByGrade(grade)

        students.forEach { student ->
            student.role = StudentRole.GRADUATE
            student.major = null
            student.studentNumber = null
            student.dormitoryRoomNumber = null
            student.majorClub = null
            student.jobClub = null
            student.autonomousClub = null
        }

        return GraduateStudentResDto(graduatedCount = students.size)
    }
}
