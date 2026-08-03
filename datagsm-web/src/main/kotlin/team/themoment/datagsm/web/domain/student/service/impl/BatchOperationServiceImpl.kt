package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangeItem
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.mapper.EventObjectMapper
import team.themoment.datagsm.common.domain.student.dto.internal.BatchOperationType
import team.themoment.datagsm.common.domain.student.dto.request.BatchOperationReqDto
import team.themoment.datagsm.common.domain.student.dto.response.GraduateStudentResDto
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.BatchOperationService

@Service
class BatchOperationServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : BatchOperationService {
    @Transactional
    override fun execute(reqDto: BatchOperationReqDto): GraduateStudentResDto =
        when (reqDto.operation) {
            BatchOperationType.GRADUATE -> graduateStudents(reqDto.filter?.grade ?: 3)
        }

    private fun graduateStudents(grade: Int): GraduateStudentResDto {
        val students = studentJpaRepository.findStudentsByGrade(grade)

        val olds =
            students.mapIndexed { index, student ->
                EventChangeItem(index, EventObjectMapper.from(student))
            }

        students.forEach { student ->
            student.role = StudentRole.GRADUATE
            student.major = null
            student.studentNumber = null
            student.dormitoryRoomNumber = null
            student.majorClub = null
            student.autonomousClub = null
        }

        val news =
            students.mapIndexed { index, student ->
                EventChangeItem(index, EventObjectMapper.from(student))
            }
        if (olds.isNotEmpty()) {
            applicationEventPublisher.publishEvent(
                EventDispatchRequested(
                    EventType.STUDENT_UPDATED,
                    EventChangedData(old = olds, new = news),
                ),
            )
        }

        return GraduateStudentResDto(graduatedCount = students.size)
    }
}
