package team.themoment.datagsm.common.global.storage

import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.global.data.ProjectIconStorageEnvironment
import team.themoment.sdk.exception.ExpectedException

/**
 * 컴포넌트 스캔 대상에서 제외한다.
 * 프로젝트 도메인을 다루지 않는 모듈(oauth)은 설정을 등록하지 않으므로,
 * 스캔으로 빈이 만들어지면 의존성을 찾지 못해 컨텍스트 초기화가 실패한다.
 * 설정을 등록하는 모듈이 ProjectIconStorageConfig로 직접 빈을 만든다.
 */
class ProjectIconUrlResolver(
    private val environment: ProjectIconStorageEnvironment,
) {
    fun toIconUrl(iconKey: String?): String? = iconKey?.let { "${environment.cdnBaseUrl.trimEnd('/')}/$it" }

    /**
     * 수정 요청에서 생략(null)하면 현재 값을 유지하고, 빈 문자열이면 삭제로 간주한다.
     * 값을 매번 다시 올리지 않아도 되도록 하되 지우는 수단은 남겨 둔다.
     */
    fun resolveIconKeyForUpdate(
        requestedIconKey: String?,
        currentIconKey: String?,
    ): String? =
        when (requestedIconKey) {
            null -> currentIconKey
            else -> validateIconKey(requestedIconKey)
        }

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
