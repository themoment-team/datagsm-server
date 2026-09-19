package team.themoment.datagsm.common.global.security.util

import java.security.MessageDigest

/**
 * 불투명 토큰을 저장·대조하기 위한 해시 유틸.
 * 발급 측과 검증 측이 서로 다른 방식을 쓰면 대조가 통째로 실패하므로 한 곳에서 관리한다.
 */
object OpaqueTokenHashUtil {
    fun hash(value: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    fun matches(
        rawValue: String,
        expectedHash: String,
    ): Boolean =
        MessageDigest.isEqual(
            hash(rawValue).toByteArray(Charsets.UTF_8),
            expectedHash.toByteArray(Charsets.UTF_8),
        )
}
