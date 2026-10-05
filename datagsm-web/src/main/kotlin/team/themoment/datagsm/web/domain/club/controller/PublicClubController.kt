package team.themoment.datagsm.web.domain.club.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.club.dto.request.QueryPublicClubReqDto
import team.themoment.datagsm.common.domain.club.dto.response.PublicClubListResDto
import team.themoment.datagsm.web.domain.club.service.QueryPublicClubService

@Tag(name = "Public Club", description = "인증 없이 조회 가능한 동아리 공개 API")
@RestController
@RequestMapping("/v1/public/clubs")
class PublicClubController(
    private val queryPublicClubService: QueryPublicClubService,
) {
    @Operation(
        summary = "공개 동아리 목록 조회",
        description = "동아리 ID·이름·종류 목록을 인증 없이 조회합니다. 프로젝트 신청과 공개 목록 필터의 선택지로 사용합니다. IP 기준 요청 제한이 적용됩니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "429", description = "요청 제한 초과", content = [Content()]),
        ],
    )
    @GetMapping
    fun getPublicClubs(
        @Valid @ModelAttribute queryReq: QueryPublicClubReqDto,
    ): PublicClubListResDto = queryPublicClubService.execute(queryReq)
}
