package team.themoment.datagsm.openapi.domain.club.service.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.dto.payload.ClubEventObject
import team.themoment.datagsm.common.domain.event.dto.payload.EmptyEventObject
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangeItem
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.dto.payload.EventStudentRef
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.openapi.domain.club.service.DeleteClubService
import team.themoment.sdk.exception.ExpectedException

@Service
class DeleteClubServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : DeleteClubService {
    @Transactional
    override fun execute(clubId: Long) {
        val club =
            clubJpaRepository
                .findByIdOrNull(clubId)
                ?: throw ExpectedException("동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        val oldMembers =
            if (club.type == ClubType.MAJOR_CLUB) {
                studentJpaRepository.findByMajorClub(club)
            } else {
                studentJpaRepository.findByAutonomousClub(club)
            }
        val oldObj = generateClubEventObject(club, club.leader, oldMembers.filter { it.id != club.leader?.id })

        studentJpaRepository.bulkClearClubReferences(listOf(club))
        clubJpaRepository.deleteAllByIdInBatch(listOf(clubId))

        applicationEventPublisher.publishEvent(
            EventDispatchRequested(
                EventType.CLUB_UPDATED,
                EventChangedData(
                    old = listOf(EventChangeItem(0, oldObj)),
                    new = listOf(EventChangeItem(0, EmptyEventObject())),
                ),
            ),
        )
    }

    private fun generateClubEventObject(
        club: ClubJpaEntity,
        leader: StudentJpaEntity?,
        participants: List<StudentJpaEntity>,
    ): ClubEventObject =
        ClubEventObject(
            clubId = club.id!!,
            name = club.name,
            type = club.type.name,
            foundedYear = club.foundedYear,
            status = club.status.name,
            abolishedYear = club.abolishedYear,
            leader = leader?.let { EventStudentRef(it.studentNumber?.fullStudentNumber, it.name) },
            participants = participants.map { EventStudentRef(it.studentNumber?.fullStudentNumber, it.name) },
        )
}
