package team.themoment.datagsm.common.global.storage

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.global.data.ProjectIconStorageEnvironment
import team.themoment.sdk.exception.ExpectedException

@Component
class ProjectIconUrlResolver(
    private val environment: ProjectIconStorageEnvironment,
) {
    fun toIconUrl(iconKey: String?): String? = iconKey?.let { "${environment.cdnBaseUrl.trimEnd('/')}/$it" }

    /**
     * 클라이언트가 보낸 key는 신뢰할 수 없으므로 서버가 발급한 형식인지 확인한다.
     * 경로 상위 이동이나 타 프리픽스 참조를 막는다.
     */
    fun validateIconKey(iconKey: String?): String? {
        if (iconKey.isNullOrBlank()) return null
        if (!ICON_KEY_REGEX.matches(iconKey)) {
            throw ExpectedException("올바르지 않은 아이콘 키입니다.", HttpStatus.BAD_REQUEST)
        }
        if (!iconKey.startsWith("${environment.keyPrefix}/")) {
            throw ExpectedException("올바르지 않은 아이콘 키입니다.", HttpStatus.BAD_REQUEST)
        }
        return iconKey
    }

    private companion object {
        val ICON_KEY_REGEX = Regex("""^[A-Za-z0-9._-]+/[0-9a-fA-F-]{36}\.(png|jpg|webp|gif)$""")
    }
}
