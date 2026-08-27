package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangeItem
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.dto.payload.StudentEventObject
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.student.dto.request.RequestStudentDataEditReqDto
import team.themoment.datagsm.common.domain.student.entity.StudentDataEditRequestJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.StudentDataEditField
import team.themoment.datagsm.common.domain.student.repository.StudentDataEditRequestJpaRepository
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.RequestStudentDataEditService
import team.themoment.sdk.exception.ExpectedException

@Service
class RequestStudentDataEditServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
    private val studentDataEditRequestJpaRepository: StudentDataEditRequestJpaRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : RequestStudentDataEditService {
    @Transactional
    override fun execute(reqDto: RequestStudentDataEditReqDto) {
        val students = studentJpaRepository.findAllById(reqDto.studentIds)

        if (students.size != reqDto.studentIds.toSet().size) {
            throw ExpectedException("존재하지 않는 학생이 포함되어 있습니다.", HttpStatus.NOT_FOUND)
        }

        val olds =
            students.mapIndexed { index, student ->
                EventChangeItem(index, generateStudentEventObject(student))
            }

        students.forEach { student -> applyFieldReset(student, reqDto.fields) }

        val existingRequests =
            studentDataEditRequestJpaRepository
                .findAllByStudentIdIn(students.mapNotNull { it.id })
                .associateBy { it.studentId }

        students.forEach { student ->
            val request = existingRequests[student.id] ?: StudentDataEditRequestJpaEntity().apply { studentId = student.id }
            mergeRequestedFields(request, reqDto.fields)
            studentDataEditRequestJpaRepository.save(request)
        }

        val news =
            students.mapIndexed { index, student ->
                EventChangeItem(index, generateStudentEventObject(student))
            }

        if (olds.isNotEmpty()) {
            applicationEventPublisher.publishEvent(
                EventDispatchRequested(
                    EventType.STUDENT_UPDATED,
                    EventChangedData(old = olds, new = news),
                ),
            )
        }
    }

    private fun applyFieldReset(
        student: StudentJpaEntity,
        fields: Set<StudentDataEditField>,
    ) {
        fields.forEach { field ->
            when (field) {
                StudentDataEditField.STUDENT_NUMBER -> student.studentNumber = null
                StudentDataEditField.DORMITORY_ROOM_NUMBER -> student.dormitoryRoomNumber = null
                StudentDataEditField.MAJOR_CLUB -> student.majorClub = null
                StudentDataEditField.AUTONOMOUS_CLUB -> student.autonomousClub = null
            }
        }
    }

    private fun mergeRequestedFields(
        request: StudentDataEditRequestJpaEntity,
        fields: Set<StudentDataEditField>,
    ) {
        fields.forEach { field ->
            when (field) {
                StudentDataEditField.STUDENT_NUMBER -> request.requestStudentNumber = true
                StudentDataEditField.DORMITORY_ROOM_NUMBER -> request.requestDormitoryRoomNumber = true
                StudentDataEditField.MAJOR_CLUB -> request.requestMajorClub = true
                StudentDataEditField.AUTONOMOUS_CLUB -> request.requestAutonomousClub = true
            }
        }
    }

    private fun generateStudentEventObject(student: StudentJpaEntity): StudentEventObject =
        StudentEventObject(
            studentId = student.id!!,
            name = student.name,
            email = student.email,
            sex = student.sex.name,
            grade = student.studentNumber?.studentGrade,
            classNum = student.studentNumber?.studentClass,
            number = student.studentNumber?.studentNumber,
            studentNumber = student.studentNumber?.fullStudentNumber,
            major = student.major?.name,
            specialty = student.specialty,
            role = student.role.name,
            dormitoryFloor = student.dormitoryRoomNumber?.dormitoryRoomFloor,
            dormitoryRoom = student.dormitoryRoomNumber?.dormitoryRoomNumber,
            majorClubName = student.majorClub?.name,
            autonomousClubName = student.autonomousClub?.name,
            githubId = student.githubId,
        )
}
