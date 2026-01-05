package team.themoment.datagsm.domain.student.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.domain.student.dto.response.StudentListResDto
import team.themoment.datagsm.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.domain.student.entity.constant.StudentSortBy
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.domain.student.service.QueryStudentService
import team.themoment.datagsm.global.common.constant.SortDirection

@Service
@Transactional(readOnly = true)
class QueryStudentServiceImpl(
    private final val studentJpaRepository: StudentJpaRepository,
) : QueryStudentService {
    override fun execute(
        studentId: Long?,
        name: String?,
        email: String?,
        grade: Int?,
        classNum: Int?,
        number: Int?,
        sex: Sex?,
        role: StudentRole?,
        dormitoryRoom: Int?,
        isLeaveSchool: Boolean?,
        page: Int,
        size: Int,
        sortBy: StudentSortBy?,
        sortDirection: SortDirection,
    ): StudentListResDto {
        val studentPage =
            studentJpaRepository.searchStudentsWithPaging(
                id = studentId,
                name = name,
                email = email,
                grade = grade,
                classNum = classNum,
                number = number,
                sex = sex,
                role = role,
                dormitoryRoom = dormitoryRoom,
                isLeaveSchool = isLeaveSchool,
                pageable = PageRequest.of(page, size),
                sortBy = sortBy,
                sortDirection = sortDirection,
            )

        return StudentListResDto(
            totalElements = studentPage.totalElements,
            totalPages = studentPage.totalPages,
            students =
                studentPage.content.map { entity ->
                    StudentResDto(
                        id = entity.id!!,
                        name = entity.name,
                        sex = entity.sex,
                        email = entity.email,
                        grade = entity.studentNumber.studentGrade,
                        classNum = entity.studentNumber.studentClass,
                        number = entity.studentNumber.studentNumber,
                        studentNumber = entity.studentNumber.fullStudentNumber,
                        major = entity.major,
                        role = entity.role,
                        dormitoryFloor = entity.dormitoryRoomNumber?.dormitoryRoomFloor,
                        dormitoryRoom = entity.dormitoryRoomNumber?.dormitoryRoomNumber,
                        isLeaveSchool = entity.isLeaveSchool,
                        majorClub = entity.majorClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
                        jobClub = entity.jobClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
                        autonomousClub = entity.autonomousClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
                    )
                },
        )
    }
}
