package team.themoment.datagsm.common.domain.oauth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 302 대신 200으로 이동할 주소를 내려준다.
 *
 * 세션 쿠키가 백엔드 호스트에 host-only로 심겨 있어 이 요청은 브라우저에서 직접 나가야 하는데,
 * 브라우저 fetch는 302를 처리할 수단이 없다. redirect를 manual로 두면 응답이 opaqueredirect가 되어
 * Location을 읽을 수 없고, follow로 두면 fetch가 클라이언트 콜백을 대신 호출해 일회용 code를 소진한다.
 * 그래서 주소를 본문으로 내려주고 최종 이동은 프론트가 수행한다.
 *
 * 대신 Location 헤더와 달리 이 주소는 프론트 JS가 읽을 수 있고, 승인 응답에는 인가 코드가 실려 있다.
 * 읽을 수 있는 출처를 CORS 허용 목록이 결정하므로, 목록을 넓힐 때는 이 점을 함께 고려해야 한다.
 */
data class OauthConsentResDto(
    @field:Schema(description = "동의 처리 후 이동할 클라이언트 주소. 승인은 code, 거부는 error=access_denied가 실려 있다.")
    val redirectUrl: String,
)
