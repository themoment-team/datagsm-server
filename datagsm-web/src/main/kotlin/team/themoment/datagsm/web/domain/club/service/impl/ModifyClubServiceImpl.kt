package team.themoment.datagsm.web.domain.club.service.impl

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
import team.themoment.datagsm.web.domain.club.service.ModifyClubService
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
        if (reqDto.status == ClubStatus.ABOLISHED && reqDto.leaderId != null) {
            throw ExpectedException("폐지된 동아리에는 부장을 지정할 수 없습니다.", HttpStatus.BAD_REQUEST)
        }
        if (reqDto.status == ClubStatus.ABOLISHED && reqDto.participantIds.isNotEmpty()) {
            throw ExpectedException("폐지된 동아리에는 구성원을 지정할 수 없습니다.", HttpStatus.BAD_REQUEST)
        }

        val club =
            clubJpaRepository
                .findByIdOrNull(clubId)
                ?: throw ExpectedException("동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        if (clubJpaRepository.existsByNameAndIdNot(reqDto.name, clubId)) {
            throw ExpectedException("이미 존재하는 동아리 이름입니다.", HttpStatus.CONFLICT)
        }

        val oldType = club.type

        club.name = reqDto.name
        club.type = reqDto.type
        club.foundedYear = reqDto.foundedYear
        club.status = reqDto.status
        club.abolishedYear = if (reqDto.status == ClubStatus.ABOLISHED) reqDto.abolishedYear else null

        val newLeader: StudentJpaEntity?
        val participants: List<StudentJpaEntity>
        val participantIdsForBulkAssign: List<Long>

        val leaderId = reqDto.leaderId
        if (leaderId != null) {
            newLeader =
                studentJpaRepository
                    .findByIdOrNull(leaderId)
                    ?: throw ExpectedException(
                        "부장으로 지정한 학생을 찾을 수 없습니다. studentId: $leaderId",
                        HttpStatus.NOT_FOUND,
                    )
            club.leader = newLeader
            val filteredParticipantIds = reqDto.participantIds.filter { it != leaderId }
            participants = studentJpaRepository.findAllById(filteredParticipantIds)
            participantIdsForBulkAssign = listOf(leaderId) + filteredParticipantIds
            val clubsToUnsetLeader =
                (listOf(newLeader) + participants)
                    .flatMap { student -> clubJpaRepository.findAllByLeader(student) }
                    .filter { it.type == reqDto.type && it.id != clubId }
            clubsToUnsetLeader.forEach { otherClub -> otherClub.leader = null }
        } else {
            newLeader = null
            club.leader = null
            participants = studentJpaRepository.findAllById(reqDto.participantIds)
            participantIdsForBulkAssign = reqDto.participantIds
        }

        studentJpaRepository.clearClubReferencesByType(club, oldType)
        if (reqDto.status != ClubStatus.ABOLISHED) {
            studentJpaRepository.bulkAssignClub(participantIdsForBulkAssign, club, reqDto.type)
        }

        return ClubResDto(
            id = club.id!!,
            name = club.name,
            type = club.type,
            leader = newLeader?.toParticipantInfoDto(),
            participants = participants.map { it.toParticipantInfoDto() },
            foundedYear = club.foundedYear,
            status = club.status,
            abolishedYear = club.abolishedYear,
        )
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
