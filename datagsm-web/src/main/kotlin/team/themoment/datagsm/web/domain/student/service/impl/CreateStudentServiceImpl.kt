package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.DormitoryRoomNumber
import team.themoment.datagsm.common.domain.student.Major
import team.themoment.datagsm.common.domain.student.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.StudentNumber
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.web.domain.student.dto.request.CreateStudentReqDto
import team.themoment.datagsm.web.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.web.domain.student.service.CreateStudentService
import team.themoment.sdk.exception.ExpectedException

@Service
@Transactional
class CreateStudentServiceImpl(
    private final val studentJpaRepository: StudentJpaRepository,
    private final val clubJpaRepository: ClubJpaRepository,
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
                val clubIds = listOfNotNull(reqDto.majorClubId, reqDto.jobClubId, reqDto.autonomousClubId)
                val clubs =
                    if (clubIds.isNotEmpty()) {
                        clubJpaRepository.findAllById(clubIds).associateBy { it.id }
                    } else {
                        emptyMap()
                    }
                majorClub =
                    reqDto.majorClubId?.let { clubId ->
                        clubs[clubId] ?: throw ExpectedException("전공 동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                    }
                jobClub =
                    reqDto.jobClubId?.let { clubId ->
                        clubs[clubId] ?: throw ExpectedException("취업 동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                    }
                autonomousClub =
                    reqDto.autonomousClubId?.let { clubId ->
                        clubs[clubId] ?: throw ExpectedException("자율 동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                    }
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
            dormitoryFloor = savedStudent.dormitoryRoomNumber?.dormitoryRoomFloor,
            dormitoryRoom = savedStudent.dormitoryRoomNumber?.dormitoryRoomNumber,
            isLeaveSchool = savedStudent.isLeaveSchool,
            majorClub = savedStudent.majorClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
            jobClub = savedStudent.jobClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
            autonomousClub = savedStudent.autonomousClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
        )
    }
}
