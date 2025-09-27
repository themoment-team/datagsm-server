package team.themoment.datagsm.domain.student.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.student.dto.internal.StudentDto
import team.themoment.datagsm.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.domain.student.service.QueryStudentService

@Service
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
        role: Role?,
        dormitoryRoom: Int?,
        isLeaveSchool: Boolean,
        page: Int,
        size: Int,
    ): StudentResDto {
        val studentPage =
            studentJpaRepository.searchStudentsWithPaging(
                studentId = studentId,
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
            )

        return StudentResDto(
            totalElements = studentPage.totalElements,
            totalPages = studentPage.totalPages,
            students =
                studentPage.content.map { entity ->
                    StudentDto(
                        studentId = entity.studentId!!,
                        name = entity.studentName,
                        sex = entity.studentSex,
                        email = entity.studentEmail,
                        grade = entity.studentNumber.studentGrade,
                        classNum = entity.studentNumber.studentClass,
                        number = entity.studentNumber.studentNumber,
                        studentNumber = entity.studentNumber.fullStudentNumber,
                        major = entity.studentMajor,
                        role = entity.studentRole,
                        dormitoryFloor = entity.studentDormitoryRoomNumber.dormitoryRoomFloor,
                        dormitoryRoom = entity.studentDormitoryRoomNumber.dormitoryRoomNumber,
                        isLeaveSchool = entity.studentIsLeaveSchool,
                    )
                },
        )
    }
}
