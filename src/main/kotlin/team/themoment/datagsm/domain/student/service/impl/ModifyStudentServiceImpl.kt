package team.themoment.datagsm.domain.student.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.student.dto.internal.StudentDto
import team.themoment.datagsm.domain.student.dto.request.StudentUpdateReqDto
import team.themoment.datagsm.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.domain.student.entity.constant.DormitoryRoomNumber
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.StudentNumber
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.domain.student.service.ModifyStudentService

@Service
@Transactional
class ModifyStudentServiceImpl(
    private final val studentJpaRepository: StudentJpaRepository,
) : ModifyStudentService {
    override fun execute(
        studentId: Long,
        reqDto: StudentUpdateReqDto,
    ): StudentResDto {
        val student =
            studentJpaRepository
                .findById(studentId)
                .orElseThrow { IllegalArgumentException("학생을 찾을 수 없습니다. studentId: $studentId") }
        reqDto.email?.let { email ->
            if (studentJpaRepository.existsByStudentEmailAndNotStudentId(email, studentId)) {
                throw IllegalArgumentException("이미 존재하는 이메일입니다: $email")
            }
            student.studentEmail = email
        }
        if (reqDto.grade != null || reqDto.classNum != null || reqDto.number != null) {
            val newGrade = reqDto.grade ?: student.studentNumber.studentGrade
            val newClassNum = reqDto.classNum ?: student.studentNumber.studentClass
            val newNumber = reqDto.number ?: student.studentNumber.studentNumber

            if (studentJpaRepository.existsByStudentNumberAndNotStudentId(
                    newGrade,
                    newClassNum,
                    newNumber,
                    studentId,
                )
            ) {
                throw IllegalArgumentException("이미 존재하는 학번입니다: ${newGrade}학년 ${newClassNum}반 ${newNumber}번")
            }

            student.studentNumber = StudentNumber(newGrade, newClassNum, newNumber)

            if (reqDto.classNum != null) {
                student.studentMajor = Major.fromClassNum(newClassNum)
                    ?: throw IllegalArgumentException("유효하지 않은 학급입니다: $newClassNum")
            }
        }

        reqDto.name?.let { student.studentName = it }
        reqDto.sex?.let { student.studentSex = it }
        reqDto.role?.let { student.studentRole = it }
        reqDto.dormitoryRoomNumber?.let {
            student.studentDormitoryRoomNumber = DormitoryRoomNumber(it)
        }

        val savedStudent = studentJpaRepository.save(student)

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
