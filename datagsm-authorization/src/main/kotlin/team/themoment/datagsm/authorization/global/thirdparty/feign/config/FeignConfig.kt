package team.themoment.datagsm.authorization.global.thirdparty.feign.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.authorization.global.thirdparty.feign.error.FeignErrorDecoder

@Configuration
@EnableFeignClients(basePackages = ["team.themoment.datagsm.authorization.global.thirdparty.feign"])
class FeignConfig {
    @Bean
    fun feignErrorDecoder(): FeignErrorDecoder = FeignErrorDecoder()
}
