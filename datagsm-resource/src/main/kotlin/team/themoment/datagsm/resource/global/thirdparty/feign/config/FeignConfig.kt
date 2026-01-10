package team.themoment.datagsm.resource.global.thirdparty.feign.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.resource.global.thirdparty.feign.error.FeignErrorDecoder

@Configuration
@EnableFeignClients(
    basePackages = [
        "team.themoment.datagsm.resource.global.thirdparty.feign",
        "team.themoment.datagsm.common.global.thirdparty.feign",
    ],
)
class FeignConfig {
    @Bean
    fun feignErrorDecoder(): FeignErrorDecoder = FeignErrorDecoder()
}
