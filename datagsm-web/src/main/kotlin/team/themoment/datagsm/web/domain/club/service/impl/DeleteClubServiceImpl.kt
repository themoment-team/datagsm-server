package team.themoment.datagsm.web.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.event.dto.payload.ClubDeletedData
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.service.EventPublisher
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.club.service.DeleteClubService
import team.themoment.sdk.exception.ExpectedException

@Service
class DeleteClubServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
    private val eventPublisher: EventPublisher,
) : DeleteClubService {
    @Transactional
    override fun execute(clubId: Long) {
        val club =
            clubJpaRepository
                .findById(clubId)
                .orElseThrow { ExpectedException("동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }
        studentJpaRepository.bulkClearClubReferences(listOf(club))
        clubJpaRepository.deleteAllByIdInBatch(listOf(clubId))

        eventPublisher.dispatch(
            EventType.CLUB_DELETED,
            ClubDeletedData(clubId = club.id!!, name = club.name),
        )
    }
}
