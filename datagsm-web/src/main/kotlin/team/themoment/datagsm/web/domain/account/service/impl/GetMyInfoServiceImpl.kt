package team.themoment.datagsm.web.domain.account.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.student.StudentJpaEntity
import team.themoment.datagsm.web.domain.account.dto.response.GetMyInfoResDto
import team.themoment.datagsm.web.domain.account.service.GetMyInfoService
import team.themoment.datagsm.web.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.web.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.web.global.exception.error.ExpectedException
import team.themoment.datagsm.web.global.security.authentication.type.AuthType
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider

@Service
class GetMyInfoServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
) : GetMyInfoService {
    @Transactional(readOnly = true)
    override fun execute(): GetMyInfoResDto {
        val principal = currentUserProvider.getPrincipal()
        if (principal.type == AuthType.API_KEY) throw ExpectedException("API Key 인증은 해당 API를 지원하지 않습니다.", HttpStatus.FORBIDDEN)
        val account = currentUserProvider.getCurrentAccount()

        return GetMyInfoResDto(
            id = account.id!!,
            email = account.email,
            role = account.role,
            isStudent = account.student != null,
            student =
                account.student?.let { student ->
                    generateStudentResDto(student)
                },
        )
    }

    private fun generateStudentResDto(student: StudentJpaEntity): StudentResDto =
        StudentResDto(
            id = student.id!!,
            name = student.name,
            sex = student.sex,
            email = student.email,
            grade = student.studentNumber.studentGrade,
            classNum = student.studentNumber.studentClass,
            number = student.studentNumber.studentNumber,
            studentNumber = student.studentNumber.fullStudentNumber,
            major = student.major,
            role = student.role,
            dormitoryFloor = student.dormitoryRoomNumber?.dormitoryRoomFloor,
            dormitoryRoom = student.dormitoryRoomNumber?.dormitoryRoomNumber,
            isLeaveSchool = student.isLeaveSchool,
            majorClub = student.majorClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
            jobClub = student.jobClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
            autonomousClub = student.autonomousClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
        )
}
