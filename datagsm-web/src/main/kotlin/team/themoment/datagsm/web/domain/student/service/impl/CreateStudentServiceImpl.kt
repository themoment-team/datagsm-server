package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.dto.request.CreateStudentReqDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.common.domain.student.entity.DormitoryRoomNumber
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.CreateStudentService
import team.themoment.sdk.exception.ExpectedException

@Service
class CreateStudentServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
    private val clubJpaRepository: ClubJpaRepository,
) : CreateStudentService {
    @Transactional
    override fun execute(reqDto: CreateStudentReqDto): StudentResDto {
        if (studentJpaRepository.existsByEmail(reqDto.email)) {
            throw ExpectedException("이미 존재하는 이메일입니다.", HttpStatus.CONFLICT)
        }

        when (reqDto.role) {
            StudentRole.GRADUATE -> {
                val hasStudentNumberInfo = reqDto.grade != null || reqDto.classNum != null || reqDto.number != null
                val hasDormitoryInfo = reqDto.dormitoryRoomNumber != null
                val hasClubInfo = reqDto.majorClubId != null || reqDto.autonomousClubId != null
                if (hasStudentNumberInfo || hasDormitoryInfo || hasClubInfo) {
                    throw ExpectedException("졸업생은 학번, 기숙사, 동아리 정보를 가질 수 없습니다.", HttpStatus.BAD_REQUEST)
                }
            }
            StudentRole.WITHDRAWN -> {
                val hasDormitoryInfo = reqDto.dormitoryRoomNumber != null
                val hasClubInfo = reqDto.majorClubId != null || reqDto.autonomousClubId != null
                if (hasDormitoryInfo || hasClubInfo) {
                    throw ExpectedException("자퇴생은 기숙사, 동아리 정보를 가질 수 없습니다.", HttpStatus.BAD_REQUEST)
                }
            }
            else -> {
                if (reqDto.grade == null || reqDto.classNum == null || reqDto.number == null) {
                    throw ExpectedException("재학생은 학번 정보(학년, 반, 번호)가 필수입니다.", HttpStatus.BAD_REQUEST)
                }
            }
        }

        if (reqDto.grade != null && reqDto.classNum != null && reqDto.number != null) {
            if (studentJpaRepository.existsByStudentNumber(reqDto.grade!!, reqDto.classNum!!, reqDto.number!!)) {
                throw ExpectedException(
                    "이미 존재하는 학번입니다.",
                    HttpStatus.CONFLICT,
                )
            }
        }

        val studentEntity =
            StudentJpaEntity().apply {
                name = reqDto.name
                sex = reqDto.sex
                email = reqDto.email
                role = reqDto.role
                specialty = reqDto.specialty
                if (reqDto.grade != null && reqDto.classNum != null && reqDto.number != null) {
                    studentNumber = StudentNumber(reqDto.grade, reqDto.classNum, reqDto.number)
                }
                if (reqDto.role != StudentRole.GRADUATE && reqDto.role != StudentRole.WITHDRAWN) {
                    major = Major.fromClassNum(reqDto.classNum!!)
                        ?: throw ExpectedException("유효하지 않은 학급 번호입니다.", HttpStatus.BAD_REQUEST)
                    dormitoryRoomNumber = DormitoryRoomNumber(reqDto.dormitoryRoomNumber)
                    val clubIds = listOfNotNull(reqDto.majorClubId, reqDto.autonomousClubId)
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
                    autonomousClub =
                        reqDto.autonomousClubId?.let { clubId ->
                            clubs[clubId] ?: throw ExpectedException("자율 동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                        }
                }
            }

        val savedStudent = studentJpaRepository.save(studentEntity)

        return StudentResDto(
            id = savedStudent.id!!,
            name = savedStudent.name,
            sex = savedStudent.sex,
            email = savedStudent.email,
            grade = savedStudent.studentNumber?.studentGrade,
            classNum = savedStudent.studentNumber?.studentClass,
            number = savedStudent.studentNumber?.studentNumber,
            studentNumber = savedStudent.studentNumber?.fullStudentNumber,
            major = savedStudent.major,
            specialty = savedStudent.specialty,
            role = savedStudent.role,
            dormitoryFloor = savedStudent.dormitoryRoomNumber?.dormitoryRoomFloor,
            dormitoryRoom = savedStudent.dormitoryRoomNumber?.dormitoryRoomNumber,
            majorClub = savedStudent.majorClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
            autonomousClub = savedStudent.autonomousClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
            githubId = savedStudent.githubId,
            githubUrl = savedStudent.githubId?.let { "https://github.com/$it" },
        )
    }
}
