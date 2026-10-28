package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.cors")
data class CorsEnvironment(
    // YAML 리스트 항목 하나에 쉼표로 넣으면 나뉘지 않으므로 쉼표로 구분한 단일 문자열로 받는다.
    // 쉼표 목록을 나눌 때 바인더가 항목마다 앞뒤 공백을 제거하므로 이 값은 이미 정규화되어 있다.
    val allowedOrigins: List<String>,
) {
    init {
        // allowedOrigins는 패턴을 지원하지 않아 "*"나 경로가 섞인 값은 어떤 요청과도 매칭되지 않으므로 기동 단계에서 막는다.
        require(allowedOrigins.isNotEmpty() && allowedOrigins.all { EXPLICIT_ORIGIN.matches(it) }) {
            "spring.security.cors.allowed-origins must be a non-empty list of explicit origins (scheme://host[:port], wildcard not allowed)"
        }
    }

    private companion object {
        // Spring이 끝의 슬래시 하나는 제거하고 비교하므로 허용한다.
        val EXPLICIT_ORIGIN = Regex("^[a-zA-Z][a-zA-Z\\d+.-]*://[^/\\s*]+/?$")
    }
}
