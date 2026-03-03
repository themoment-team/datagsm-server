package team.themoment.datagsm.oauth.userinfo.domain.userinfo.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.dto.response.AccountInfoResDto
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.oauth.userinfo.domain.userinfo.service.QueryUserInfoService
import team.themoment.datagsm.oauth.userinfo.global.security.provider.CurrentUserProvider

@Service
class QueryUserInfoServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
) : QueryUserInfoService {
    @Transactional(readOnly = true)
    override fun execute(): AccountInfoResDto {
        val account = currentUserProvider.getCurrentAccount()

        return AccountInfoResDto(
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
            grade = student.studentNumber?.studentGrade,
            classNum = student.studentNumber?.studentClass,
            number = student.studentNumber?.studentNumber,
            studentNumber = student.studentNumber?.fullStudentNumber,
            major = student.major,
            role = student.role,
            dormitoryFloor = student.dormitoryRoomNumber?.dormitoryRoomFloor,
            dormitoryRoom = student.dormitoryRoomNumber?.dormitoryRoomNumber,
            majorClub = student.majorClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
            jobClub = student.jobClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
            autonomousClub = student.autonomousClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
        )
}
