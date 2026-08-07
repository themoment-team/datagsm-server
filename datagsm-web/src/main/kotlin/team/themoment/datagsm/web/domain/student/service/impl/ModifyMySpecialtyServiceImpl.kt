package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangeItem
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.dto.payload.StudentEventObject
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.web.domain.student.dto.request.UpdateMySpecialtyReqDto
import team.themoment.datagsm.web.domain.student.service.ModifyMySpecialtyService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider

@Service
class ModifyMySpecialtyServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : ModifyMySpecialtyService {
    @Transactional
    override fun execute(reqDto: UpdateMySpecialtyReqDto) {
        val student = currentUserProvider.getCurrentStudent()

        val oldObj = generateStudentEventObject(student)

        student.specialty = reqDto.specialty

        val newObj = generateStudentEventObject(student)
        applicationEventPublisher.publishEvent(
            EventDispatchRequested(
                EventType.STUDENT_UPDATED,
                EventChangedData(
                    old = listOf(EventChangeItem(0, oldObj)),
                    new = listOf(EventChangeItem(0, newObj)),
                ),
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
