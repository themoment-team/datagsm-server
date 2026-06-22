package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.dto.payload.StudentChangedData
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.service.EventPublisher
import team.themoment.datagsm.common.domain.student.dto.internal.BatchOperationType
import team.themoment.datagsm.common.domain.student.dto.request.BatchOperationReqDto
import team.themoment.datagsm.common.domain.student.dto.response.GraduateStudentResDto
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.mapper.StudentEventSnapshotMapper
import team.themoment.datagsm.web.domain.student.service.BatchOperationService

@Service
class BatchOperationServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
    private val eventPublisher: EventPublisher,
    private val snapshotMapper: StudentEventSnapshotMapper,
) : BatchOperationService {
    @Transactional
    override fun execute(reqDto: BatchOperationReqDto): GraduateStudentResDto =
        when (reqDto.operation) {
            BatchOperationType.GRADUATE -> graduateStudents(reqDto.filter?.grade ?: 3)
        }

    private fun graduateStudents(grade: Int): GraduateStudentResDto {
        val students = studentJpaRepository.findStudentsByGrade(grade)

        val olds = snapshotMapper.toSnapshots(students)

        students.forEach { student ->
            student.role = StudentRole.GRADUATE
            student.major = null
            student.studentNumber = null
            student.dormitoryRoomNumber = null
            student.majorClub = null
            student.autonomousClub = null
        }

        val news = snapshotMapper.toSnapshots(students)
        if (olds.isNotEmpty()) {
            eventPublisher.dispatch(
                EventType.STUDENT_CHANGED,
                StudentChangedData(old = olds, new = news),
            )
        }

        return GraduateStudentResDto(graduatedCount = students.size)
    }
}
