package team.themoment.datagsm.web.domain.event.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.event.dto.request.CreateEventReqDto
import team.themoment.datagsm.common.domain.event.dto.request.ModifyEventReqDto
import team.themoment.datagsm.common.domain.event.dto.response.CreateEventResDto
import team.themoment.datagsm.common.domain.event.dto.response.EventListResDto
import team.themoment.datagsm.common.domain.event.dto.response.EventResDto
import team.themoment.datagsm.web.domain.event.service.CreateEventService
import team.themoment.datagsm.web.domain.event.service.DeleteEventService
import team.themoment.datagsm.web.domain.event.service.ModifyEventService
import team.themoment.datagsm.web.domain.event.service.QueryEventService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "Event", description = "Event 관련 API")
@RestController
@RequestMapping("/v1/events")
class EventController(
    private val createEventService: CreateEventService,
    private val queryEventService: QueryEventService,
    private val modifyEventService: ModifyEventService,
    private val deleteEventService: DeleteEventService,
) {
    @Operation(summary = "Event 등록", description = "새로운 Event를 등록합니다. secret은 이 응답에서만 확인할 수 있습니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "등록 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패 또는 Event 최대 개수 초과)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "인증 실패", content = [Content()]),
        ],
    )
    @PostMapping
    fun createEvent(
        @RequestBody @Valid reqDto: CreateEventReqDto,
    ): CreateEventResDto = createEventService.execute(reqDto)

    @Operation(summary = "Event 목록 조회", description = "현재 로그인한 사용자의 Event 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패", content = [Content()]),
        ],
    )
    @GetMapping
    fun getEvents(): EventListResDto = queryEventService.execute()

    @Operation(summary = "Event 수정", description = "Event의 수신 URL 또는 구독 이벤트를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "인증 실패", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Event를 찾을 수 없음", content = [Content()]),
        ],
    )
    @PatchMapping("/{eventId}")
    fun updateEvent(
        @Parameter(description = "Event ID") @PathVariable eventId: Long,
        @RequestBody @Valid reqDto: ModifyEventReqDto,
    ): EventResDto = modifyEventService.execute(eventId, reqDto)

    @Operation(summary = "Event 삭제", description = "등록된 Event를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Event를 찾을 수 없음", content = [Content()]),
        ],
    )
    @DeleteMapping("/{eventId}")
    fun deleteEvent(
        @Parameter(description = "Event ID") @PathVariable eventId: Long,
    ): CommonApiResponse<Nothing> {
        deleteEventService.execute(eventId)
        return CommonApiResponse.success("Event를 성공적으로 삭제했습니다.")
    }
}
