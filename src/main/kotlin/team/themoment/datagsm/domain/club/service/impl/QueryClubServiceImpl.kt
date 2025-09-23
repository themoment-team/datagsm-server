package team.themoment.datagsm.domain.club.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.service.QueryClubService

@Service
class QueryClubServiceImpl : QueryClubService {
    override fun execute(
        clubId: Long?,
        clubName: String?,
        clubType: ClubType?,
        page: Int,
        size: Int
    ) {
        TODO("Not yet implemented")
    }

}
