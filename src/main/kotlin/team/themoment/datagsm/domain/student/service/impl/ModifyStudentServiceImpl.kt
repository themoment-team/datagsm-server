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
        if (studentJpaRepository.existsByStudentEmailAndNotId(reqDto.email, studentId)) {
            throw ExpectedException("이미 존재하는 이메일입니다: ${reqDto.email}", HttpStatus.CONFLICT)
        }
        if (studentJpaRepository.existsByStudentNumberAndNotId(
                reqDto.grade,
                reqDto.classNum,
                reqDto.number,
                studentId,
            )
        ) {
            throw ExpectedException(
                "이미 존재하는 학번입니다: ${reqDto.grade}학년 ${reqDto.classNum}반 ${reqDto.number}번",
                HttpStatus.CONFLICT,
            )
        }
        val major =
            Major.fromClassNum(reqDto.classNum)
                ?: throw ExpectedException("유효하지 않은 학급입니다: ${reqDto.classNum}", HttpStatus.BAD_REQUEST)
        student.name = reqDto.name
        student.sex = reqDto.sex
        student.email = reqDto.email
        student.studentNumber = StudentNumber(reqDto.grade, reqDto.classNum, reqDto.number)
        student.major = major
        student.role = reqDto.role
        student.isLeaveSchool = reqDto.isLeaveSchool
        student.dormitoryRoomNumber = DormitoryRoomNumber(reqDto.dormitoryRoomNumber)
        val clubIds = listOfNotNull(reqDto.majorClubId, reqDto.jobClubId, reqDto.autonomousClubId)
        val clubs =
            if (clubIds.isNotEmpty()) {
                clubJpaRepository.findAllById(clubIds).associateBy { it.id }
            } else {
                emptyMap()
            }
        student.majorClub =
            reqDto.majorClubId?.let { clubId ->
                clubs[clubId] ?: throw ExpectedException("전공 동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            }
        student.jobClub =
            reqDto.jobClubId?.let { clubId ->
                clubs[clubId] ?: throw ExpectedException("취업 동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            }
        student.autonomousClub =
            reqDto.autonomousClubId?.let { clubId ->
                clubs[clubId] ?: throw ExpectedException("자율 동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
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
            dormitoryFloor = student.dormitoryRoomNumber?.dormitoryRoomFloor,
            dormitoryRoom = student.dormitoryRoomNumber?.dormitoryRoomNumber,
            isLeaveSchool = student.isLeaveSchool,
            majorClub = student.majorClub?.let { ClubResDto(id = it.id!!, name = it.name, type = it.type) },
            jobClub = student.jobClub?.let { ClubResDto(id = it.id!!, name = it.name, type = it.type) },
            autonomousClub = student.autonomousClub?.let { ClubResDto(id = it.id!!, name = it.name, type = it.type) },
        )
    }
}
