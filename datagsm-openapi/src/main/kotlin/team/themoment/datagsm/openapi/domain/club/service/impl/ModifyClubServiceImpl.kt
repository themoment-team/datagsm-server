package team.themoment.datagsm.openapi.domain.club.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.common.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.openapi.domain.club.service.ModifyClubService
import team.themoment.sdk.exception.ExpectedException

@Service
class ModifyClubServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
) : ModifyClubService {
    @Transactional
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

        studentJpaRepository.clearClubReferencesByType(club, oldType)

        val filteredParticipantIds = reqDto.participantIds.filter { it != reqDto.leaderId }
        val participants = studentJpaRepository.findAllById(filteredParticipantIds)

        assignClubToStudent(newLeader, club, reqDto.type)
        participants.forEach { assignClubToStudent(it, club, reqDto.type) }

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

    private fun assignClubToStudent(
        student: StudentJpaEntity,
        club: ClubJpaEntity,
        type: ClubType,
    ) {
        when (type) {
            ClubType.MAJOR_CLUB -> student.majorClub = club
            ClubType.JOB_CLUB -> student.jobClub = club
            ClubType.AUTONOMOUS_CLUB -> student.autonomousClub = club
        }
    }
}
