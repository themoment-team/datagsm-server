package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 아이콘 조회에 필요한 cdnBaseUrl은 모든 모듈이 사용하고,
 * 업로드 관련 항목은 presigned URL을 발급하는 모듈에서만 사용한다.
 */
@ConfigurationProperties(prefix = "storage.project-icon")
data class ProjectIconStorageEnvironment(
    val cdnBaseUrl: String,
    val bucket: String = "",
    val region: String = "",
    val keyPrefix: String = "project-icons",
    val uploadUrlExpirationSeconds: Long = 300,
    val maxUploadSizeBytes: Long = 5 * 1024 * 1024,
    val allowedContentTypes: Set<String> =
        setOf("image/png", "image/jpeg", "image/webp", "image/gif"),
)
