package team.themoment.datagsm.openapi.global.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
import team.themoment.datagsm.common.global.data.NeisEnvironment
import team.themoment.datagsm.common.global.data.ProjectIconStorageEnvironment
import team.themoment.datagsm.common.global.storage.ProjectIconUrlResolver

@Configuration
@EnableConfigurationProperties(
    ApiKeyEnvironment::class,
    NeisEnvironment::class,
    ProjectIconStorageEnvironment::class,
)
class PropertiesScanConfig {
    @Bean
    fun projectIconUrlResolver(projectIconStorageEnvironment: ProjectIconStorageEnvironment): ProjectIconUrlResolver =
        ProjectIconUrlResolver(projectIconStorageEnvironment)
}
