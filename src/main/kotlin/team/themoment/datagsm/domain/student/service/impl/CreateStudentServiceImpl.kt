package team.themoment.datagsm.domain.student.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.student.dto.request.CreateStudentReqDto
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
    override fun execute(reqDto: CreateStudentReqDto): StudentResDto {
        if (studentJpaRepository.existsByEmail(reqDto.email)) {
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
                name = reqDto.name
                sex = reqDto.sex
                email = reqDto.email
                studentNumber = StudentNumber(reqDto.grade, reqDto.classNum, reqDto.number)
                major = Major.fromClassNum(reqDto.classNum)
                    ?: throw ExpectedException("유효하지 않은 학급입니다: ${reqDto.classNum}", HttpStatus.BAD_REQUEST)
                role = reqDto.role
                dormitoryRoomNumber = DormitoryRoomNumber(reqDto.dormitoryRoomNumber)
            }

        val savedStudent = studentJpaRepository.save(studentEntity)

        return StudentResDto(
            id = savedStudent.id!!,
            name = savedStudent.name,
            sex = savedStudent.sex,
            email = savedStudent.email,
            grade = savedStudent.studentNumber.studentGrade,
            classNum = savedStudent.studentNumber.studentClass,
            number = savedStudent.studentNumber.studentNumber,
            studentNumber = savedStudent.studentNumber.fullStudentNumber,
            major = savedStudent.major,
            role = savedStudent.role,
            dormitoryFloor = savedStudent.dormitoryRoomNumber.dormitoryRoomFloor,
            dormitoryRoom = savedStudent.dormitoryRoomNumber.dormitoryRoomNumber,
            isLeaveSchool = savedStudent.isLeaveSchool,
        )
    }
}
