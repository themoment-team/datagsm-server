package team.themoment.datagsm.authorization.global.thirdparty.feign.resource

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import team.themoment.datagsm.authorization.global.thirdparty.feign.resource.dto.StudentResDto

@FeignClient(
    name = "resource-server",
    url = "\${spring.security.resource-server.url:http://localhost:8082}",
)
interface ResourceServerClient {
    @GetMapping("/v1/students")
    fun getStudentByEmail(
        @RequestHeader("X-API-KEY") apiKey: String,
        @RequestParam email: String,
    ): List<StudentResDto>
}
