package team.themoment.datagsm.common.global.security.util

import org.springframework.http.ResponseCookie
import team.themoment.datagsm.common.global.data.OauthEnvironment
import java.time.Duration

/**
 * IdP 세션 쿠키를 만드는 유일한 지점.
 *
 * 브라우저는 name/path/domain이 모두 일치해야 기존 쿠키를 덮어쓴다.
 * 발급과 만료를 각각 따로 조립하면 속성이 어긋나는 순간 로그아웃이 조용히 실패하므로
 * 두 형태를 같은 자리에서 만든다.
 *
 * 세션 쿠키를 읽는 곳은 같은 호스트의 인가 엔드포인트뿐이라 host-only로 둔다.
 * Domain을 상위 도메인으로 넓히면 모든 서브도메인이 세션 쿠키를 받게 되어,
 * 서브도메인 하나가 침해되면 SSO 세션 전체가 넘어간다.
 */
object IdpSessionCookieFactory {
    fun issued(
        oauthEnvironment: OauthEnvironment,
        sessionId: String,
    ): ResponseCookie =
        baseBuilder(oauthEnvironment, sessionId)
            .maxAge(Duration.ofSeconds(oauthEnvironment.idpSessionExpirationSeconds))
            .build()

    fun expired(oauthEnvironment: OauthEnvironment): ResponseCookie =
        baseBuilder(oauthEnvironment, "")
            .maxAge(Duration.ZERO)
            .build()

    private fun baseBuilder(
        oauthEnvironment: OauthEnvironment,
        value: String,
    ): ResponseCookie.ResponseCookieBuilder =
        ResponseCookie
            .from(oauthEnvironment.idpSessionCookieName, value)
            .httpOnly(true)
            .secure(oauthEnvironment.idpSessionCookieSecure)
            .sameSite("Lax")
            .path("/")
}
