package team.themoment.datagsm.domain.health

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.global.common.response.dto.response.CommonApiResponse

@RestController
@RequestMapping("/v1/health")
class HealthController {
    @GetMapping
    fun checkHealth(): CommonApiResponse<Nothing> = CommonApiResponse.success("OK")
}
