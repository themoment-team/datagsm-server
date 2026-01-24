package team.themoment.datagsm.resource.domain.club.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.response.ClubListResDto
import team.themoment.datagsm.common.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubSortBy
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.common.global.constant.SortDirection
import team.themoment.datagsm.resource.domain.club.service.QueryClubService

@Service
@Transactional
class QueryClubServiceImpl(
    private final val clubJpaRepository: ClubJpaRepository,
    private final val studentJpaRepository: StudentJpaRepository,
) : QueryClubService {
    override fun execute(
        clubId: Long?,
        clubName: String?,
        clubType: ClubType?,
        page: Int,
        size: Int,
        includeLeaderInParticipants: Boolean,
        sortBy: ClubSortBy?,
        sortDirection: SortDirection,
    ): ClubListResDto {
        val clubPage =
            clubJpaRepository.searchClubWithPaging(
                id = clubId,
                name = clubName,
                type = clubType,
                pageable = PageRequest.of(page, size),
                sortBy = sortBy,
                sortDirection = sortDirection,
            )

        return ClubListResDto(
            totalPages = clubPage.totalPages,
            totalElements = clubPage.totalElements,
            clubs =
                clubPage.content.map { entity ->
                    val participants = getParticipantsByClubType(entity)
                    val leader = entity.leader.toParticipantInfoDto()
                    val participantList =
                        if (includeLeaderInParticipants) {
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
            ClubType.MAJOR_CLUB -> studentJpaRepository.findByMajorClub(club)
            ClubType.JOB_CLUB -> studentJpaRepository.findByJobClub(club)
            ClubType.AUTONOMOUS_CLUB -> studentJpaRepository.findByAutonomousClub(club)
        }

    private fun StudentJpaEntity.toParticipantInfoDto() =
        ParticipantInfoDto(
            id = this.id!!,
            name = this.name,
            email = this.email,
            studentNumber = this.studentNumber.fullStudentNumber,
            major = this.major,
            sex = this.sex,
        )
}
