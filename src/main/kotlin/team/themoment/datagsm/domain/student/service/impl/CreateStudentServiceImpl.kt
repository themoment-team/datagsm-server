package team.themoment.datagsm.domain.student.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.student.dto.internal.StudentDto
import team.themoment.datagsm.domain.student.dto.request.StudentCreateReqDto
import team.themoment.datagsm.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.DormitoryRoomNumber
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.StudentNumber
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.domain.student.service.CreateStudentService

@Service
@Transactional
class CreateStudentServiceImpl(
    private final val studentJpaRepository: StudentJpaRepository,
) : CreateStudentService {
    override fun createStudent(reqDto: StudentCreateReqDto): StudentResDto {
        if (studentJpaRepository.existsByStudentEmail(reqDto.email)) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다: ${reqDto.email}")
        }

        if (studentJpaRepository.existsByStudentNumber(reqDto.grade, reqDto.classNum, reqDto.number)) {
            throw IllegalArgumentException("이미 존재하는 학번입니다: ${reqDto.grade}학년 ${reqDto.classNum}반 ${reqDto.number}번")
        }

        val studentEntity =
            StudentJpaEntity().apply {
                studentName = reqDto.name
                studentSex = reqDto.sex
                studentEmail = reqDto.email
                studentNumber = StudentNumber(reqDto.grade, reqDto.classNum, reqDto.number)
                studentMajor = Major.fromGrade(reqDto.classNum)
                    ?: throw IllegalArgumentException("유효하지 않은 학급입니다: ${reqDto.classNum}")
                studentRole = reqDto.role
                studentDormitoryRoomNumber = DormitoryRoomNumber(reqDto.dormitoryRoomNumber)
            }

        val savedStudent = studentJpaRepository.save(studentEntity)

        val studentDto =
            StudentDto(
                studentId = savedStudent.studentId!!,
                name = savedStudent.studentName,
                sex = savedStudent.studentSex,
                email = savedStudent.studentEmail,
                grade = savedStudent.studentNumber.studentGrade,
                classNum = savedStudent.studentNumber.studentClass,
                number = savedStudent.studentNumber.studentNumber,
                studentNumber = savedStudent.studentNumber.fullStudentNumber,
                major = savedStudent.studentMajor,
                role = savedStudent.studentRole,
                dormitoryFloor = savedStudent.studentDormitoryRoomNumber.dormitoryRoomFloor,
                dormitoryRoom = savedStudent.studentDormitoryRoomNumber.dormitoryRoomNumber,
                isLeaveSchool = savedStudent.studentIsLeaveSchool,
            )

        return StudentResDto(
            totalPages = 1,
            totalElements = 1L,
            students = listOf(studentDto),
        )
    }
}
