package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.student.dto.request.UpdateStudentStatusReqDto
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.UpdateStudentStatusService
import team.themoment.sdk.exception.ExpectedException

@Service
class UpdateStudentStatusServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
) : UpdateStudentStatusService {
    @Transactional
    override fun execute(
        studentId: Long,
        reqDto: UpdateStudentStatusReqDto,
    ) {
        val student =
            studentJpaRepository.findByIdOrNull(studentId)
                ?: throw ExpectedException("학생을 찾을 수 없습니다. ID: $studentId", HttpStatus.NOT_FOUND)

        when (reqDto.status) {
            StudentRole.GRADUATE -> {
                student.role = StudentRole.GRADUATE
                student.major = null
                student.studentNumber = null
                student.dormitoryRoomNumber = null
                student.majorClub = null
                student.jobClub = null
                student.autonomousClub = null
            }
            StudentRole.WITHDRAWN -> {
                student.role = StudentRole.WITHDRAWN
                student.major = null
                student.studentNumber = null
                student.dormitoryRoomNumber = null
                student.majorClub = null
                student.jobClub = null
                student.autonomousClub = null
            }
            else -> throw ExpectedException("지원하지 않는 상태입니다: ${reqDto.status}", HttpStatus.BAD_REQUEST)
        }
    }
}
