package team.themoment.datagsm.web.domain.club.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.common.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.club.service.ModifyClubService
import team.themoment.sdk.exception.ExpectedException

@Service
@Transactional
class ModifyClubServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
) : ModifyClubService {
    override fun execute(
        clubId: Long,
        reqDto: ClubReqDto,
    ): ClubResDto {
        val club =
            clubJpaRepository
                .findByIdOrNull(clubId)
                ?: throw ExpectedException("동아리를 찾을 수 없습니다. clubId: $clubId", HttpStatus.NOT_FOUND)
        if (clubJpaRepository.existsByNameAndIdNot(reqDto.name, clubId)) {
            throw ExpectedException("이미 존재하는 동아리 이름입니다: ${reqDto.name}", HttpStatus.CONFLICT)
        }

        val newLeader =
            studentJpaRepository
                .findByIdOrNull(reqDto.leaderId)
                ?: throw ExpectedException(
                    "부장으로 지정한 학생을 찾을 수 없습니다. studentId: ${reqDto.leaderId}",
                    HttpStatus.NOT_FOUND,
                )

        val oldType = club.type
        club.name = reqDto.name
        club.type = reqDto.type
        club.leader = newLeader

        // 동아리 최대 인원이 30명 이하이므로 bulk DML 대신 엔티티 직접 수정을 사용
        // Bulk 연산을 수행할 성능적 이점이 사실상 없다고 판단하였습니다
        val oldParticipants =
            when (oldType) {
                ClubType.MAJOR_CLUB -> studentJpaRepository.findByMajorClub(club)
                ClubType.AUTONOMOUS_CLUB -> studentJpaRepository.findByAutonomousClub(club)
            }
        oldParticipants.forEach { student ->
            when (oldType) {
                ClubType.MAJOR_CLUB -> student.majorClub = null
                ClubType.AUTONOMOUS_CLUB -> student.autonomousClub = null
            }
        }

        val filteredParticipantIds = reqDto.participantIds.filter { it != reqDto.leaderId }
        val participants = studentJpaRepository.findAllById(filteredParticipantIds)

        (listOf(newLeader) + participants).forEach { student ->
            when (reqDto.type) {
                ClubType.MAJOR_CLUB -> student.majorClub = club
                ClubType.AUTONOMOUS_CLUB -> student.autonomousClub = club
            }
        }

        return ClubResDto(
            id = club.id!!,
            name = club.name,
            type = club.type,
            leader =
                ParticipantInfoDto(
                    id = newLeader.id!!,
                    name = newLeader.name,
                    email = newLeader.email,
                    studentNumber = newLeader.studentNumber?.fullStudentNumber,
                    major = newLeader.major,
                    sex = newLeader.sex,
                ),
            participants =
                participants.map { student ->
                    ParticipantInfoDto(
                        id = student.id!!,
                        name = student.name,
                        email = student.email,
                        studentNumber = student.studentNumber?.fullStudentNumber,
                        major = student.major,
                        sex = student.sex,
                    )
                },
        )
    }
}
