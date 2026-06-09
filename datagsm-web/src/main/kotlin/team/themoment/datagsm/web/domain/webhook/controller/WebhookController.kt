package team.themoment.datagsm.web.domain.webhook.controller

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
import team.themoment.datagsm.common.domain.webhook.dto.request.CreateWebhookReqDto
import team.themoment.datagsm.common.domain.webhook.dto.request.ModifyWebhookReqDto
import team.themoment.datagsm.common.domain.webhook.dto.response.CreateWebhookResDto
import team.themoment.datagsm.common.domain.webhook.dto.response.WebhookListResDto
import team.themoment.datagsm.common.domain.webhook.dto.response.WebhookResDto
import team.themoment.datagsm.web.domain.webhook.service.CreateWebhookService
import team.themoment.datagsm.web.domain.webhook.service.DeleteWebhookService
import team.themoment.datagsm.web.domain.webhook.service.ModifyWebhookService
import team.themoment.datagsm.web.domain.webhook.service.QueryWebhookService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "Webhook", description = "Webhook 관련 API")
@RestController
@RequestMapping("/v1/webhooks")
class WebhookController(
    private val createWebhookService: CreateWebhookService,
    private val queryWebhookService: QueryWebhookService,
    private val modifyWebhookService: ModifyWebhookService,
    private val deleteWebhookService: DeleteWebhookService,
) {
    @Operation(summary = "Webhook 등록", description = "새로운 Webhook을 등록합니다. secret은 이 응답에서만 확인할 수 있습니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "등록 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패 또는 Webhook 최대 개수 초과)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "인증 실패", content = [Content()]),
        ],
    )
    @PostMapping
    fun createWebhook(
        @RequestBody @Valid reqDto: CreateWebhookReqDto,
    ): CreateWebhookResDto = createWebhookService.execute(reqDto)

    @Operation(summary = "Webhook 목록 조회", description = "현재 로그인한 사용자의 Webhook 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패", content = [Content()]),
        ],
    )
    @GetMapping
    fun getWebhooks(): WebhookListResDto = queryWebhookService.execute()

    @Operation(summary = "Webhook 수정", description = "Webhook의 수신 URL 또는 구독 이벤트를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "인증 실패", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Webhook을 찾을 수 없음", content = [Content()]),
        ],
    )
    @PatchMapping("/{webhookId}")
    fun updateWebhook(
        @Parameter(description = "Webhook ID") @PathVariable webhookId: Long,
        @RequestBody @Valid reqDto: ModifyWebhookReqDto,
    ): WebhookResDto = modifyWebhookService.execute(webhookId, reqDto)

    @Operation(summary = "Webhook 삭제", description = "등록된 Webhook을 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Webhook을 찾을 수 없음", content = [Content()]),
        ],
    )
    @DeleteMapping("/{webhookId}")
    fun deleteWebhook(
        @Parameter(description = "Webhook ID") @PathVariable webhookId: Long,
    ): CommonApiResponse<Nothing> {
        deleteWebhookService.execute(webhookId)
        return CommonApiResponse.success("Webhook을 성공적으로 삭제했습니다.")
    }
}
