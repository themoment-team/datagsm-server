package team.themoment.datagsm.web.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import team.themoment.datagsm.common.global.data.ProjectIconStorageEnvironment

@Configuration
class S3Config(
    private val projectIconStorageEnvironment: ProjectIconStorageEnvironment,
) {
    @Bean(destroyMethod = "close")
    fun s3Presigner(): S3Presigner =
        S3Presigner
            .builder()
            .region(Region.of(projectIconStorageEnvironment.region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build()
}
