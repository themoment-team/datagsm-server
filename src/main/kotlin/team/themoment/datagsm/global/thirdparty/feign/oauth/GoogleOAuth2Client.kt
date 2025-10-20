package team.themoment.datagsm.global.thirdparty.feign.oauth

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import team.themoment.datagsm.global.thirdparty.feign.config.FeignConfig
import team.themoment.datagsm.global.thirdparty.feign.oauth.dto.GoogleTokenResDto

@FeignClient(
    name = "google-oauth2-client",
    url = "https://oauth2.googleapis.com",
    configuration = [FeignConfig::class],
)
interface GoogleOAuth2Client {
    @PostMapping(value = ["/token"], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun exchangeCodeForToken(
        @RequestBody formParams: Map<String, String>,
    ): GoogleTokenResDto
}
