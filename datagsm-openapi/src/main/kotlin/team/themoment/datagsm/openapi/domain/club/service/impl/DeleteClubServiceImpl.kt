package team.themoment.datagsm.openapi.domain.club.service.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.dto.payload.EmptyEventObject
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangeItem
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.mapper.EventObjectMapper
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
        val oldObj = EventObjectMapper.from(club, club.leader, oldMembers.filter { it.id != club.leader?.id })

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
}
