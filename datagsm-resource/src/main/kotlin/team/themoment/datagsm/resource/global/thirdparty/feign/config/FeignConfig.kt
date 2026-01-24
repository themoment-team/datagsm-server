package team.themoment.datagsm.resource.global.thirdparty.feign.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.common.global.thirdparty.feign.discord.DiscordWebhookClient
import team.themoment.datagsm.resource.global.thirdparty.feign.error.FeignErrorDecoder
import team.themoment.datagsm.resource.global.thirdparty.feign.neis.NeisApiClient

@Configuration
@EnableFeignClients(
    basePackageClasses = [
        NeisApiClient::class,
        DiscordWebhookClient::class,
    ],
)
class FeignConfig {
    @Bean
    fun feignErrorDecoder(): FeignErrorDecoder = FeignErrorDecoder()
}
