package team.themoment.datagsm.openapi.domain.student.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentListResDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.common.domain.student.entity.EnrolledStudent
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.entity.constant.StudentSortBy
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.common.global.constant.SortDirection
import team.themoment.datagsm.openapi.domain.student.service.QueryStudentService

@Service
class QueryStudentServiceImpl(
    private final val studentJpaRepository: StudentJpaRepository,
) : QueryStudentService {
    @Transactional(readOnly = true)
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
        includeGraduates: Boolean,
        page: Int,
        size: Int,
        sortBy: StudentSortBy?,
        sortDirection: SortDirection,
    ): StudentListResDto {
        val studentPage =
            studentJpaRepository.searchRegisteredStudentsWithPaging(
                id = studentId,
                name = name,
                email = email,
                grade = grade,
                classNum = classNum,
                number = number,
                sex = sex,
                role = role,
                dormitoryRoom = dormitoryRoom,
                includeGraduates = includeGraduates,
                pageable = PageRequest.of(page, size),
                sortBy = sortBy,
                sortDirection = sortDirection,
            )

        return StudentListResDto(
            totalElements = studentPage.totalElements,
            totalPages = studentPage.totalPages,
            students =
                studentPage.content.map { entity ->
                    when (entity) {
                        is EnrolledStudent ->
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
                                majorClub = entity.majorClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
                                jobClub = entity.jobClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
                                autonomousClub =
                                    entity.autonomousClub?.let {
                                        ClubSummaryDto(
                                            id = it.id!!,
                                            name = it.name,
                                            type = it.type,
                                        )
                                    },
                            )
                        else ->
                            StudentResDto(
                                id = entity.id!!,
                                name = entity.name,
                                sex = entity.sex,
                                email = entity.email,
                                grade = null,
                                classNum = null,
                                number = null,
                                studentNumber = null,
                                major = null,
                                role = entity.role,
                                dormitoryFloor = null,
                                dormitoryRoom = null,
                                majorClub = null,
                                jobClub = null,
                                autonomousClub = null,
                            )
                    }
                },
        )
    }
}
