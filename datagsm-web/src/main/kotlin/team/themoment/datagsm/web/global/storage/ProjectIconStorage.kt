package team.themoment.datagsm.web.global.storage

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import team.themoment.datagsm.common.global.data.ProjectIconStorageEnvironment
import team.themoment.datagsm.common.global.storage.ProjectIconUrlResolver
import team.themoment.sdk.exception.ExpectedException
import java.time.Duration
import java.util.UUID

@Component
class ProjectIconStorage(
    private val s3Presigner: S3Presigner,
    private val environment: ProjectIconStorageEnvironment,
    private val projectIconUrlResolver: ProjectIconUrlResolver,
) {
    fun createUploadUrl(
        contentType: String,
        contentLength: Long,
    ): ProjectIconUploadTarget {
        val normalizedContentType = contentType.trim().lowercase()
        if (normalizedContentType !in environment.allowedContentTypes) {
            throw ExpectedException("지원하지 않는 이미지 형식입니다.", HttpStatus.BAD_REQUEST)
        }
        if (contentLength > environment.maxUploadSizeBytes) {
            throw ExpectedException("이미지 크기가 허용 범위를 초과했습니다.", HttpStatus.BAD_REQUEST)
        }

        val iconKey = "${environment.keyPrefix}/${UUID.randomUUID()}.${resolveExtension(normalizedContentType)}"
        val expiration = Duration.ofSeconds(environment.uploadUrlExpirationSeconds)

        // contentType/contentLength를 서명에 포함해 다른 형식·크기로의 업로드를 S3가 거부하도록 한다
        val presignedRequest =
            s3Presigner.presignPutObject(
                PutObjectPresignRequest
                    .builder()
                    .signatureDuration(expiration)
                    .putObjectRequest(
                        PutObjectRequest
                            .builder()
                            .bucket(environment.bucket)
                            .key(iconKey)
                            .contentType(normalizedContentType)
                            .contentLength(contentLength)
                            .build(),
                    ).build(),
            )

        return ProjectIconUploadTarget(
            uploadUrl = presignedRequest.url().toExternalForm(),
            iconKey = iconKey,
            expiresInSeconds = expiration.seconds,
        )
    }

    fun validateIconKey(iconKey: String?): String? = projectIconUrlResolver.validateIconKey(iconKey)

    fun toIconUrl(iconKey: String?): String? = projectIconUrlResolver.toIconUrl(iconKey)

    private fun resolveExtension(contentType: String): String =
        when (contentType) {
            "image/png" -> "png"
            "image/jpeg" -> "jpg"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            else -> throw ExpectedException("지원하지 않는 이미지 형식입니다.", HttpStatus.BAD_REQUEST)
        }
}
