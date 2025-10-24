package team.themoment.datagsm.domain.student.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.student.dto.request.StudentCreateReqDto
import team.themoment.datagsm.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.DormitoryRoomNumber
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.StudentNumber
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.domain.student.service.CreateStudentService
import team.themoment.datagsm.global.exception.error.ExpectedException

@Service
@Transactional
class CreateStudentServiceImpl(
    private final val studentJpaRepository: StudentJpaRepository,
) : CreateStudentService {
    override fun execute(reqDto: StudentCreateReqDto): StudentResDto {
        if (studentJpaRepository.existsByStudentEmail(reqDto.email)) {
            throw ExpectedException("이미 존재하는 이메일입니다: ${reqDto.email}", HttpStatus.CONFLICT)
        }

        if (studentJpaRepository.existsByStudentNumber(reqDto.grade, reqDto.classNum, reqDto.number)) {
            throw ExpectedException(
                "이미 존재하는 학번입니다: ${reqDto.grade}학년 ${reqDto.classNum}반 ${reqDto.number}번",
                HttpStatus.CONFLICT,
            )
        }

        val studentEntity =
            StudentJpaEntity().apply {
                studentName = reqDto.name
                studentSex = reqDto.sex
                studentEmail = reqDto.email
                studentNumber = StudentNumber(reqDto.grade, reqDto.classNum, reqDto.number)
                studentMajor = Major.fromClassNum(reqDto.classNum)
                    ?: throw IllegalArgumentException("유효하지 않은 학급입니다: ${reqDto.classNum}")
                studentRole = reqDto.role
                studentDormitoryRoomNumber = DormitoryRoomNumber(reqDto.dormitoryRoomNumber)
            }

        val savedStudent = studentJpaRepository.save(studentEntity)

        return StudentResDto(
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
    }
}
