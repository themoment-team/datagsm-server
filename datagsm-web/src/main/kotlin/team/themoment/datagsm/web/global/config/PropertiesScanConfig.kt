package team.themoment.datagsm.web.global.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
import team.themoment.datagsm.common.global.data.CorsEnvironment
import team.themoment.datagsm.common.global.data.IconUploadRateLimitEnvironment
import team.themoment.datagsm.common.global.data.ProjectIconStorageEnvironment
import team.themoment.datagsm.common.global.data.PublicApiRateLimitEnvironment
import team.themoment.datagsm.common.global.storage.ProjectIconUrlResolver
import team.themoment.datagsm.web.global.data.OauthJwtVerificationEnvironment

@Configuration
@EnableConfigurationProperties(
    ApiKeyEnvironment::class,
    CorsEnvironment::class,
    IconUploadRateLimitEnvironment::class,
    OauthJwtVerificationEnvironment::class,
    ProjectIconStorageEnvironment::class,
    PublicApiRateLimitEnvironment::class,
)
class PropertiesScanConfig {
    @Bean
    fun projectIconUrlResolver(projectIconStorageEnvironment: ProjectIconStorageEnvironment): ProjectIconUrlResolver =
        ProjectIconUrlResolver(projectIconStorageEnvironment)
}
