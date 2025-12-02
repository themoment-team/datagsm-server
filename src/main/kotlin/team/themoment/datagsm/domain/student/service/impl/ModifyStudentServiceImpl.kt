package team.themoment.datagsm.domain.student.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
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
    private final val clubJpaRepository: ClubJpaRepository,
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
            if (studentJpaRepository.existsByStudentEmailAndNotId(email, studentId)) {
                throw ExpectedException("이미 존재하는 이메일입니다: $email", HttpStatus.CONFLICT)
            }
            student.email = email
        }
        if (reqDto.grade != null || reqDto.classNum != null || reqDto.number != null) {
            val newGrade = reqDto.grade ?: student.studentNumber.studentGrade
            val newClassNum = reqDto.classNum ?: student.studentNumber.studentClass
            val newNumber = reqDto.number ?: student.studentNumber.studentNumber
            if (studentJpaRepository.existsByStudentNumberAndNotId(
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
                student.major = Major.fromClassNum(newClassNum)
                    ?: throw ExpectedException("유효하지 않은 학급입니다: $newClassNum", HttpStatus.BAD_REQUEST)
            }
        }
        reqDto.name?.let { student.name = it }
        reqDto.sex?.let { student.sex = it }
        reqDto.role?.let { student.role = it }
        reqDto.dormitoryRoomNumber?.let {
            student.dormitoryRoomNumber = DormitoryRoomNumber(it)
        }
        val clubIds = listOfNotNull(reqDto.majorClubId, reqDto.jobClubId, reqDto.autonomousClubId)
        val clubs =
            if (clubIds.isNotEmpty()) {
                clubJpaRepository.findAllById(clubIds).associateBy { it.id }
            } else {
                emptyMap()
            }
        reqDto.majorClubId?.let { clubId ->
            student.majorClub = clubs[clubId] ?: throw ExpectedException("전공 동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
        reqDto.jobClubId?.let { clubId ->
            student.jobClub = clubs[clubId] ?: throw ExpectedException("취업 동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
        reqDto.autonomousClubId?.let { clubId ->
            student.autonomousClub = clubs[clubId] ?: throw ExpectedException("자율 동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
        return StudentResDto(
            id = student.id!!,
            name = student.name,
            sex = student.sex,
            email = student.email,
            grade = student.studentNumber.studentGrade,
            classNum = student.studentNumber.studentClass,
            number = student.studentNumber.studentNumber,
            studentNumber = student.studentNumber.fullStudentNumber,
            major = student.major,
            role = student.role,
            dormitoryFloor = student.dormitoryRoomNumber.dormitoryRoomFloor,
            dormitoryRoom = student.dormitoryRoomNumber.dormitoryRoomNumber,
            isLeaveSchool = student.isLeaveSchool,
            majorClub = student.majorClub?.let { ClubResDto(id = it.id!!, name = it.name, type = it.type) },
            jobClub = student.jobClub?.let { ClubResDto(id = it.id!!, name = it.name, type = it.type) },
            autonomousClub = student.autonomousClub?.let { ClubResDto(id = it.id!!, name = it.name, type = it.type) },
        )
    }
}
