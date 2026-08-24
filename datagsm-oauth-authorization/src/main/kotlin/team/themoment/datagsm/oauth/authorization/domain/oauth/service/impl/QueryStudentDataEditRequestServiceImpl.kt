package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.student.dto.request.QueryStudentDataEditRequestReqDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentDataEditRequestResDto
import team.themoment.datagsm.common.domain.student.entity.constant.StudentDataEditField
import team.themoment.datagsm.common.domain.student.repository.StudentDataEditRequestJpaRepository
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.QueryStudentDataEditRequestService
import team.themoment.sdk.exception.ExpectedException

@Service
class QueryStudentDataEditRequestServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
    private val studentDataEditRequestJpaRepository: StudentDataEditRequestJpaRepository,
    private val passwordEncoder: PasswordEncoder,
) : QueryStudentDataEditRequestService {
    @Transactional(readOnly = true)
    override fun execute(reqDto: QueryStudentDataEditRequestReqDto): StudentDataEditRequestResDto {
        val account =
            accountJpaRepository
                .findByEmail(reqDto.email)
                .orElseThrow { ExpectedException("이메일 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED) }

        if (!passwordEncoder.matches(reqDto.password, account.password)) {
            throw ExpectedException("이메일 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED)
        }

        if (account.objectType != AccountObjectType.STUDENT || account.objectId == null) {
            throw ExpectedException("학생 정보가 연결되지 않은 계정입니다.", HttpStatus.FORBIDDEN)
        }

        val editRequest =
            studentDataEditRequestJpaRepository
                .findByStudentId(account.objectId!!)
                .orElseThrow { ExpectedException("진행 중인 정보 수정 요청이 없습니다.", HttpStatus.NOT_FOUND) }

        val fields = mutableSetOf<StudentDataEditField>()
        if (editRequest.requestStudentNumber) fields.add(StudentDataEditField.STUDENT_NUMBER)
        if (editRequest.requestDormitoryRoomNumber) fields.add(StudentDataEditField.DORMITORY_ROOM_NUMBER)
        if (editRequest.requestMajorClub) fields.add(StudentDataEditField.MAJOR_CLUB)
        if (editRequest.requestAutonomousClub) fields.add(StudentDataEditField.AUTONOMOUS_CLUB)

        return StudentDataEditRequestResDto(fields = fields)
    }
}
