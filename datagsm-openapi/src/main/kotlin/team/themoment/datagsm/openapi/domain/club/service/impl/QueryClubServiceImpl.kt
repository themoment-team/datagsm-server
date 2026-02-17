package team.themoment.datagsm.openapi.domain.club.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.request.QueryClubReqDto
import team.themoment.datagsm.common.domain.club.dto.response.ClubListResDto
import team.themoment.datagsm.common.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.openapi.domain.club.service.QueryClubService

@Service
@Transactional
class QueryClubServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
) : QueryClubService {
    override fun execute(queryReq: QueryClubReqDto): ClubListResDto {
        val clubPage =
            clubJpaRepository.searchClubWithPaging(
                id = queryReq.clubId,
                name = queryReq.clubName,
                type = queryReq.clubType,
                pageable = PageRequest.of(queryReq.page, queryReq.size),
                sortBy = queryReq.sortBy,
                sortDirection = queryReq.sortDirection,
            )

        return ClubListResDto(
            totalPages = clubPage.totalPages,
            totalElements = clubPage.totalElements,
            clubs =
                clubPage.content.map { entity ->
                    val participants = getParticipantsByClubType(entity)
                    val leader = entity.leader.toParticipantInfoDto()
                    val participantList =
                        if (queryReq.includeLeaderInParticipants) {
                            participants.map { it.toParticipantInfoDto() }
                        } else {
                            participants
                                .filter { it.id != entity.leader.id }
                                .map { it.toParticipantInfoDto() }
                        }

                    ClubResDto(
                        id = entity.id!!,
                        name = entity.name,
                        type = entity.type,
                        leader = leader,
                        participants = participantList,
                    )
                },
        )
    }

    private fun getParticipantsByClubType(club: ClubJpaEntity): List<StudentJpaEntity> =
        when (club.type) {
            ClubType.MAJOR_CLUB -> studentJpaRepository.findRegisteredStudentsByMajorClub(club)
            ClubType.JOB_CLUB -> studentJpaRepository.findRegisteredStudentsByJobClub(club)
            ClubType.AUTONOMOUS_CLUB -> studentJpaRepository.findRegisteredStudentsByAutonomousClub(club)
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
