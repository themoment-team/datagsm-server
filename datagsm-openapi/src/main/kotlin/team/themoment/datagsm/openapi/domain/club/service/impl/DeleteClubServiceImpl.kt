package team.themoment.datagsm.openapi.domain.club.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.openapi.domain.club.service.DeleteClubService
import team.themoment.sdk.exception.ExpectedException

@Service
class DeleteClubServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
) : DeleteClubService {
    @Transactional
    override fun execute(clubId: Long) {
        val club =
            clubJpaRepository
                .findByIdOrNull(clubId)
                ?: throw ExpectedException("동아리를 찾을 수 없습니다. clubId: $clubId", HttpStatus.NOT_FOUND)
        clubJpaRepository.delete(club)
    }
}
