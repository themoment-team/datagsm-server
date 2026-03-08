package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.student.dto.request.UpdateStudentStatusReqDto
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.ModifyStudentStatusService
import team.themoment.sdk.exception.ExpectedException

@Service
class ModifyStudentStatusServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
) : ModifyStudentStatusService {
    @Transactional
    override fun execute(
        studentId: Long,
        reqDto: UpdateStudentStatusReqDto,
    ) {
        val student =
            studentJpaRepository.findByIdOrNull(studentId)
                ?: throw ExpectedException("학생을 찾을 수 없습니다. ID: $studentId", HttpStatus.NOT_FOUND)

        when (reqDto.status) {
            StudentRole.GRADUATE, StudentRole.WITHDRAWN -> {
                student.role = reqDto.status
                student.major = null
                student.studentNumber = null
                student.dormitoryRoomNumber = null
                student.majorClub = null
                student.autonomousClub = null
            }
            StudentRole.GENERAL_STUDENT, StudentRole.STUDENT_COUNCIL, StudentRole.DORMITORY_MANAGER -> {
                student.role = reqDto.status
            }
        }
    }
}
