package team.themoment.datagsm.openapi.domain.club.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.common.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
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
        when (reqDto.status) {
            ClubStatus.ACTIVE ->
                if (reqDto.leaderId == null) {
                    throw ExpectedException("운영 중인 동아리에는 부장을 지정해야 합니다.", HttpStatus.BAD_REQUEST)
                }
            ClubStatus.ABOLISHED ->
                if (reqDto.leaderId != null) {
                    throw ExpectedException("폐지된 동아리에는 부장을 지정할 수 없습니다.", HttpStatus.BAD_REQUEST)
                }
        }

        val club =
            clubJpaRepository
                .findByIdOrNull(clubId)
                ?: throw ExpectedException("동아리를 찾을 수 없습니다. clubId: $clubId", HttpStatus.NOT_FOUND)
        if (clubJpaRepository.existsByNameAndIdNot(reqDto.name, clubId)) {
            throw ExpectedException("이미 존재하는 동아리 이름입니다: ${reqDto.name}", HttpStatus.CONFLICT)
        }

        val oldType = club.type

        club.name = reqDto.name
        club.type = reqDto.type
        club.foundedYear = reqDto.foundedYear
        club.status = reqDto.status
        club.abolishedYear = if (reqDto.status == ClubStatus.ABOLISHED) reqDto.abolishedYear else null

        return when (reqDto.status) {
            ClubStatus.ACTIVE -> {
                val newLeader =
                    studentJpaRepository
                        .findByIdOrNull(reqDto.leaderId!!)
                        ?: throw ExpectedException(
                            "부장으로 지정한 학생을 찾을 수 없습니다. studentId: ${reqDto.leaderId}",
                            HttpStatus.NOT_FOUND,
                        )
                club.leader = newLeader

                studentJpaRepository.clearClubReferencesByType(club, oldType)

                val leaderIdNonNull: Long = reqDto.leaderId!!
                val filteredParticipantIds = reqDto.participantIds.filter { it != leaderIdNonNull }
                val participants = studentJpaRepository.findAllById(filteredParticipantIds)

                studentJpaRepository.bulkAssignClub(listOf(leaderIdNonNull) + filteredParticipantIds, club, reqDto.type)

                ClubResDto(
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
                        participants.map { student -> student.toParticipantInfoDto() },
                    foundedYear = club.foundedYear,
                    status = club.status,
                    abolishedYear = club.abolishedYear,
                )
            }
            ClubStatus.ABOLISHED -> {
                club.leader = null

                studentJpaRepository.clearClubReferencesByType(club, oldType)

                val filteredParticipantIds = reqDto.participantIds
                val participants = studentJpaRepository.findAllById(filteredParticipantIds)

                studentJpaRepository.bulkAssignClub(filteredParticipantIds, club, reqDto.type)

                ClubResDto(
                    id = club.id!!,
                    name = club.name,
                    type = club.type,
                    leader = null,
                    participants =
                        participants.map { student -> student.toParticipantInfoDto() },
                    foundedYear = club.foundedYear,
                    status = club.status,
                    abolishedYear = club.abolishedYear,
                )
            }
        }
    }

    private fun StudentJpaEntity.toParticipantInfoDto() =
        ParticipantInfoDto(
            id = this.id!!,
            name = this.name,
            email = this.email,
            studentNumber = this.studentNumber?.fullStudentNumber,
            major = this.major,
            sex = this.sex,
        )
}
