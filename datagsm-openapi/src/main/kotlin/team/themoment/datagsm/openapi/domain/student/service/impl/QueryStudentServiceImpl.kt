package team.themoment.datagsm.openapi.domain.student.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.student.dto.request.QueryStudentReqDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentListResDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.openapi.domain.student.service.QueryStudentService

@Service
class QueryStudentServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
) : QueryStudentService {
    @Transactional(readOnly = true)
    override fun execute(queryReq: QueryStudentReqDto): StudentListResDto {
        val studentPage =
            studentJpaRepository.searchRegisteredStudentsWithPaging(
                id = queryReq.studentId,
                name = queryReq.name,
                email = queryReq.email,
                grade = queryReq.grade,
                classNum = queryReq.classNum,
                number = queryReq.number,
                sex = queryReq.sex,
                role = queryReq.role,
                dormitoryRoom = queryReq.dormitoryRoom,
                specialty = queryReq.specialty,
                major = queryReq.major,
                githubId = queryReq.githubId,
                includeGraduates = queryReq.includeGraduates,
                includeWithdrawn = queryReq.includeWithdrawn,
                onlyEnrolled = queryReq.onlyEnrolled,
                pageable = PageRequest.of(queryReq.page, queryReq.size),
                sortBy = queryReq.sortBy,
                sortDirection = queryReq.sortDirection,
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
                        grade = entity.studentNumber?.studentGrade,
                        classNum = entity.studentNumber?.studentClass,
                        number = entity.studentNumber?.studentNumber,
                        studentNumber = entity.studentNumber?.fullStudentNumber,
                        major = entity.major,
                        specialty = entity.specialty,
                        role = entity.role,
                        dormitoryFloor = entity.dormitoryRoomNumber?.dormitoryRoomFloor,
                        dormitoryRoom = entity.dormitoryRoomNumber?.dormitoryRoomNumber,
                        majorClub = entity.majorClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
                        autonomousClub = entity.autonomousClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
                        githubId = entity.githubId,
                        githubUrl = entity.githubId?.let { "https://github.com/$it" },
                    )
                },
        )
    }
}
