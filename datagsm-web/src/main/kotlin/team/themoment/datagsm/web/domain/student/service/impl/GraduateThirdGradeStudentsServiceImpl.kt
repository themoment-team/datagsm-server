package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.dto.payload.StudentChangedData
import team.themoment.datagsm.common.domain.event.dto.payload.StudentEventSnapshot
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.service.EventPublisher
import team.themoment.datagsm.common.domain.student.dto.response.GraduateStudentResDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
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

        val olds = thirdGradeStudents.mapIndexed { index, student -> generateStudentEventSnapshot(index, student) }

        thirdGradeStudents.forEach { student ->
            student.role = StudentRole.GRADUATE
            student.major = null
            student.specialty = null
            student.studentNumber = null
            student.dormitoryRoomNumber = null
            student.majorClub = null
            student.autonomousClub = null
        }

        val news = thirdGradeStudents.mapIndexed { index, student -> generateStudentEventSnapshot(index, student) }
        if (olds.isNotEmpty()) {
            eventPublisher.dispatch(
                EventType.STUDENT_CHANGED,
                StudentChangedData(old = olds, new = news),
            )
        }

        return GraduateStudentResDto(graduatedCount = thirdGradeStudents.size)
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
