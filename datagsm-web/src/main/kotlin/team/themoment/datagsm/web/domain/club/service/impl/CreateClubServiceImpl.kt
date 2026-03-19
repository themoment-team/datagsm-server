package team.themoment.datagsm.web.domain.club.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.common.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.club.service.CreateClubService
import team.themoment.sdk.exception.ExpectedException

@Service
class CreateClubServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
) : CreateClubService {
    @Transactional
    override fun execute(clubReqDto: ClubReqDto): ClubResDto {
        if (clubReqDto.status == ClubStatus.ABOLISHED && clubReqDto.leaderId != null) {
            throw ExpectedException("폐지된 동아리에는 부장을 지정할 수 없습니다.", HttpStatus.BAD_REQUEST)
        }
        if (clubReqDto.status == ClubStatus.ABOLISHED && clubReqDto.participantIds.isNotEmpty()) {
            throw ExpectedException("폐지된 동아리에는 구성원을 지정할 수 없습니다.", HttpStatus.BAD_REQUEST)
        }

        if (clubJpaRepository.existsByName(clubReqDto.name)) {
            throw ExpectedException("이미 존재하는 동아리 이름입니다: ${clubReqDto.name}", HttpStatus.CONFLICT)
        }

        val clubEntity =
            ClubJpaEntity().apply {
                name = clubReqDto.name
                type = clubReqDto.type
                foundedYear = clubReqDto.foundedYear
                status = clubReqDto.status
                abolishedYear = if (clubReqDto.status == ClubStatus.ABOLISHED) clubReqDto.abolishedYear else null
            }

        val leader: StudentJpaEntity?
        val participants: List<StudentJpaEntity>
        val participantIdsForBulkAssign: List<Long>

        val leaderId = clubReqDto.leaderId
        if (leaderId != null) {
            leader =
                studentJpaRepository
                    .findByIdOrNull(leaderId)
                    ?: throw ExpectedException(
                        "부장으로 지정한 학생을 찾을 수 없습니다. studentId: $leaderId",
                        HttpStatus.NOT_FOUND,
                    )
            clubEntity.leader = leader
            val filteredParticipantIds = clubReqDto.participantIds.filter { it != leaderId }
            participants = studentJpaRepository.findAllById(filteredParticipantIds)
            participantIdsForBulkAssign = listOf(leaderId) + filteredParticipantIds
        } else {
            leader = null
            clubEntity.leader = null
            participants = studentJpaRepository.findAllById(clubReqDto.participantIds)
            participantIdsForBulkAssign = clubReqDto.participantIds
        }

        val savedClub = clubJpaRepository.save(clubEntity)

        if (leader != null) {
            (listOf(leader) + participants).forEach { student ->
                clubJpaRepository
                    .findAllByLeader(student)
                    .filter { it.type == clubReqDto.type && it.id != savedClub.id }
                    .forEach { otherClub -> otherClub.leader = null }
            }
        }

        studentJpaRepository.bulkAssignClub(participantIdsForBulkAssign, savedClub, clubReqDto.type)

        return ClubResDto(
            id = savedClub.id!!,
            name = savedClub.name,
            type = savedClub.type,
            leader = leader?.toParticipantInfoDto(),
            participants = participants.map { it.toParticipantInfoDto() },
            foundedYear = savedClub.foundedYear,
            status = savedClub.status,
            abolishedYear = savedClub.abolishedYear,
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
