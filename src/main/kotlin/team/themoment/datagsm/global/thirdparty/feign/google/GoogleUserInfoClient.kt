package team.themoment.datagsm.global.thirdparty.feign.google

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import team.themoment.datagsm.global.thirdparty.feign.config.FeignConfig
import team.themoment.datagsm.global.thirdparty.feign.google.dto.GoogleUserInfoResDto

@FeignClient(
    name = "google-userinfo-client",
    url = "https://www.googleapis.com",
    configuration = [FeignConfig::class]
)
interface GoogleUserInfoClient {

    @GetMapping("/oauth2/v2/userinfo")
    fun getUserInfo(@RequestHeader("Authorization") authorization: String): GoogleUserInfoResDto
}