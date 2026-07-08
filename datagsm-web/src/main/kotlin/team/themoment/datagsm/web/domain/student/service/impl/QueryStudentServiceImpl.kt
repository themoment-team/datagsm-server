package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.student.dto.request.QueryStudentReqDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentListResDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.QueryStudentService

@Service
class QueryStudentServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
) : QueryStudentService {
    @Transactional(readOnly = true)
    override fun execute(queryReq: QueryStudentReqDto): StudentListResDto {
        val studentPage =
            studentJpaRepository.searchStudentsWithPaging(
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
            students = studentPage.content.map { StudentResDto.from(it) },
        )
    }
}
