package team.themoment.datagsm.domain.club.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.service.QueryClubService

@RestController
@RequestMapping("/v1/clubs")
class ClubController(
    private val queryClubService: QueryClubService,
) {
    @GetMapping("/")
    fun getClubInfo(
        @RequestParam(required = false) clubId: Long?,
        @RequestParam(required = false) clubName: String?,
        @RequestParam(required = false) clubType: ClubType?,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "100") size: Int,
    ) = queryClubService.execute(clubId, clubName, clubType, page, size)


}
