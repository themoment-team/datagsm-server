package team.themoment.datagsm.domain.club.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.club.service.DeleteClubService

@Service
@Transactional
class DeleteClubServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
) : DeleteClubService {
    override fun execute(clubId: Long) {
        val club =
            clubJpaRepository
                .findById(clubId)
                .orElseThrow { IllegalArgumentException("동아리를 찾을 수 없습니다. clubId: $clubId") }
        clubJpaRepository.delete(club)
    }
}
