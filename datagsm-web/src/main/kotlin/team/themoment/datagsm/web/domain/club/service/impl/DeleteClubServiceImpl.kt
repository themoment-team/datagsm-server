package team.themoment.datagsm.web.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.web.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.web.domain.club.service.DeleteClubService
import team.themoment.datagsm.web.global.exception.error.ExpectedException

@Service
@Transactional
class DeleteClubServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
) : DeleteClubService {
    override fun execute(clubId: Long) {
        val club =
            clubJpaRepository
                .findById(clubId)
                .orElseThrow { ExpectedException("동아리를 찾을 수 없습니다. clubId: $clubId", HttpStatus.NOT_FOUND) }
        clubJpaRepository.delete(club)
    }
}
