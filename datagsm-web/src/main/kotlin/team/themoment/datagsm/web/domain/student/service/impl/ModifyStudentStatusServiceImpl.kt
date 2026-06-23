package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.event.dto.payload.StudentChangedData
import team.themoment.datagsm.common.domain.event.dto.payload.StudentEventSnapshot
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.service.EventPublisher
import team.themoment.datagsm.common.domain.student.dto.request.UpdateStudentStatusReqDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.ModifyStudentStatusService
import team.themoment.sdk.exception.ExpectedException

@Service
class ModifyStudentStatusServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
    private val clubJpaRepository: ClubJpaRepository,
    private val eventPublisher: EventPublisher,
) : ModifyStudentStatusService {
    @Transactional
    override fun execute(
        studentId: Long,
        reqDto: UpdateStudentStatusReqDto,
    ) {
        val student =
            studentJpaRepository.findByIdOrNull(studentId)
                ?: throw ExpectedException("학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        val old = generateStudentEventSnapshot(0, student)

        when (reqDto.status) {
            StudentRole.GRADUATE, StudentRole.WITHDRAWN -> {
                clubJpaRepository.findAllByLeader(student).forEach { it.leader = null }
                student.role = reqDto.status
                student.major = null
                student.studentNumber = null
                student.dormitoryRoomNumber = null
                student.majorClub = null
                student.autonomousClub = null
            }
            StudentRole.GENERAL_STUDENT, StudentRole.STUDENT_COUNCIL, StudentRole.DORMITORY_MANAGER -> {
                student.role = reqDto.status
            }
        }

        val new = generateStudentEventSnapshot(0, student)
        eventPublisher.dispatch(
            EventType.STUDENT_CHANGED,
            StudentChangedData(old = listOf(old), new = listOf(new)),
        )
    }

    private fun generateStudentEventSnapshot(
        index: Int,
        student: StudentJpaEntity,
    ): StudentEventSnapshot =
        StudentEventSnapshot(
            index = index,
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
