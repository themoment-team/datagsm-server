package team.themoment.datagsm.domain.club.controller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.domain.club.dto.internal.ClubDto
import team.themoment.datagsm.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.domain.club.dto.response.ClubListResDto
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.service.CreateClubService
import team.themoment.datagsm.domain.club.service.DeleteClubService
import team.themoment.datagsm.domain.club.service.ModifyClubService
import team.themoment.datagsm.domain.club.service.QueryClubService

@RestController
@RequestMapping("/v1/clubs")
class ClubController(
    private val queryClubService: QueryClubService,
    private val createClubService: CreateClubService,
    private val modifyClubService: ModifyClubService,
    private val deleteClubService: DeleteClubService,
) {
    @GetMapping
    fun getClubInfo(
        @RequestParam(required = false) clubId: Long?,
        @RequestParam(required = false) clubName: String?,
        @RequestParam(required = false) clubType: ClubType?,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "100") size: Int,
    ): ClubListResDto = queryClubService.execute(clubId, clubName, clubType, page, size)

    @PostMapping
    fun createClub(
        @RequestBody @Valid clubReqDto: ClubReqDto,
    ): ClubDto = createClubService.execute(clubReqDto)

    @PatchMapping("/{clubId}")
    fun updateClub(
        @PathVariable clubId: Long,
        @RequestBody @Valid clubReqDto: ClubReqDto,
    ): ClubDto = modifyClubService.execute(clubId, clubReqDto)

    @DeleteMapping("/{clubId}")
    fun deleteClub(
        @PathVariable clubId: Long,
    ) = deleteClubService.execute(clubId)
}
