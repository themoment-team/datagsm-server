package team.themoment.datagsm.domain.student.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.student.dto.request.UpdateStudentReqDto
import team.themoment.datagsm.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.domain.student.entity.constant.DormitoryRoomNumber
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.StudentNumber
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.domain.student.service.ModifyStudentService
import team.themoment.datagsm.global.exception.error.ExpectedException

@Service
@Transactional
class ModifyStudentServiceImpl(
    private final val studentJpaRepository: StudentJpaRepository,
) : ModifyStudentService {
    override fun execute(
        studentId: Long,
        reqDto: UpdateStudentReqDto,
    ): StudentResDto {
        val student =
            studentJpaRepository
                .findById(studentId)
                .orElseThrow { ExpectedException("학생을 찾을 수 없습니다. studentId: $studentId", HttpStatus.NOT_FOUND) }
        reqDto.email?.let { email ->
            if (studentJpaRepository.existsByStudentEmailAndNotStudentId(email, studentId)) {
                throw ExpectedException("이미 존재하는 이메일입니다: $email", HttpStatus.CONFLICT)
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
                throw ExpectedException(
                    "이미 존재하는 학번입니다: ${newGrade}학년 ${newClassNum}반 ${newNumber}번",
                    HttpStatus.CONFLICT,
                )
            }
            student.studentNumber = StudentNumber(newGrade, newClassNum, newNumber)
            if (reqDto.classNum != null) {
                student.studentMajor = Major.fromClassNum(newClassNum)
                    ?: throw ExpectedException("유효하지 않은 학급입니다: $newClassNum", HttpStatus.BAD_REQUEST)
            }
        }
        reqDto.name?.let { student.studentName = it }
        reqDto.sex?.let { student.studentSex = it }
        reqDto.role?.let { student.studentRole = it }
        reqDto.dormitoryRoomNumber?.let {
            student.studentDormitoryRoomNumber = DormitoryRoomNumber(it)
        }
        return StudentResDto(
            studentId = student.studentId!!,
            name = student.studentName,
            sex = student.studentSex,
            email = student.studentEmail,
            grade = student.studentNumber.studentGrade,
            classNum = student.studentNumber.studentClass,
            number = student.studentNumber.studentNumber,
            studentNumber = student.studentNumber.fullStudentNumber,
            major = student.studentMajor,
            role = student.studentRole,
            dormitoryFloor = student.studentDormitoryRoomNumber.dormitoryRoomFloor,
            dormitoryRoom = student.studentDormitoryRoomNumber.dormitoryRoomNumber,
            isLeaveSchool = student.studentIsLeaveSchool,
        )
    }
}
