package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.dto.request.QueryStudentDataEditRequestReqDto
import team.themoment.datagsm.common.domain.student.dto.response.DataEditFieldResDto
import team.themoment.datagsm.common.domain.student.dto.response.DataEditOptionResDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentDataEditRequestResDto
import team.themoment.datagsm.common.domain.student.entity.StudentDataEditRequestJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.StudentDataEditField
import team.themoment.datagsm.common.domain.student.repository.StudentDataEditRequestJpaRepository
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.QueryStudentDataEditRequestService
import team.themoment.sdk.exception.ExpectedException

@Service
class QueryStudentDataEditRequestServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
    private val studentDataEditRequestJpaRepository: StudentDataEditRequestJpaRepository,
    private val clubJpaRepository: ClubJpaRepository,
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

        return StudentDataEditRequestResDto(fields = buildFields(editRequest))
    }

    private fun buildFields(editRequest: StudentDataEditRequestJpaEntity): List<DataEditFieldResDto> {
        val fields = mutableListOf<DataEditFieldResDto>()
        if (editRequest.requestStudentNumber) {
            fields.add(DataEditFieldResDto(name = StudentDataEditField.STUDENT_NUMBER))
        }
        if (editRequest.requestDormitoryRoomNumber) {
            fields.add(DataEditFieldResDto(name = StudentDataEditField.DORMITORY_ROOM_NUMBER))
        }
        if (editRequest.requestMajorClub) {
            fields.add(
                DataEditFieldResDto(
                    name = StudentDataEditField.MAJOR_CLUB,
                    options = queryClubOptions(ClubType.MAJOR_CLUB),
                ),
            )
        }
        if (editRequest.requestAutonomousClub) {
            fields.add(
                DataEditFieldResDto(
                    name = StudentDataEditField.AUTONOMOUS_CLUB,
                    options = queryClubOptions(ClubType.AUTONOMOUS_CLUB),
                ),
            )
        }
        return fields
    }

    private fun queryClubOptions(type: ClubType): List<DataEditOptionResDto> =
        clubJpaRepository
            .findByType(type)
            .filter { it.status == ClubStatus.ACTIVE }
            .map(::toOption)

    private fun toOption(club: ClubJpaEntity): DataEditOptionResDto = DataEditOptionResDto(value = club.id!!, label = club.name)
}
