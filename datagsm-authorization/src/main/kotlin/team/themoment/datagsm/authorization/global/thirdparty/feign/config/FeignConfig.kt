package team.themoment.datagsm.authorization.global.thirdparty.feign.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.authorization.global.thirdparty.feign.error.FeignErrorDecoder
import team.themoment.datagsm.common.global.thirdparty.feign.discord.DiscordWebhookClient

@Configuration
@EnableFeignClients(
    basePackageClasses = [
        DiscordWebhookClient::class,
    ],
)
class FeignConfig {
    @Bean
    fun feignErrorDecoder(): FeignErrorDecoder = FeignErrorDecoder()
}
