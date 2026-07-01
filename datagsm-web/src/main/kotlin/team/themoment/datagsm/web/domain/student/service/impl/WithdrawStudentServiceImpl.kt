package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangeItem
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.dto.payload.StudentEventObject
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.service.EventPublisher
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.WithdrawStudentService
import team.themoment.sdk.exception.ExpectedException

@Service
class WithdrawStudentServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
    private val clubJpaRepository: ClubJpaRepository,
    private val eventPublisher: EventPublisher,
) : WithdrawStudentService {
    @Transactional
    override fun execute(studentId: Long) {
        val student =
            studentJpaRepository.findByIdOrNull(studentId)
                ?: throw ExpectedException("학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        clubJpaRepository.findAllByLeader(student).forEach { it.leader = null }

        val old = generateStudentEventObject(student)

        student.apply {
            role = StudentRole.WITHDRAWN
            major = null
            specialty = null
            studentNumber = null
            dormitoryRoomNumber = null
            majorClub = null
            autonomousClub = null
        }

        val new = generateStudentEventObject(student)
        eventPublisher.dispatch(
            EventType.STUDENT_UPDATED,
            EventChangedData(
                old = listOf(EventChangeItem(0, old)),
                new = listOf(EventChangeItem(0, new)),
            ),
        )
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
