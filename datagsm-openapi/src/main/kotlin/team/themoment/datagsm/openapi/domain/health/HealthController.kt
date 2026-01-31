package team.themoment.datagsm.openapi.domain.health

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "Health", description = "서버 상태 확인 API")
@RestController
@RequestMapping("/v1/health")
class HealthController {
    @Operation(summary = "서버 상태 확인", description = "서버가 정상적으로 작동하는지 확인합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "서버 정상"),
        ],
    )
    @GetMapping
    fun checkHealth(): CommonApiResponse<Nothing> = CommonApiResponse.success("OK")
}
