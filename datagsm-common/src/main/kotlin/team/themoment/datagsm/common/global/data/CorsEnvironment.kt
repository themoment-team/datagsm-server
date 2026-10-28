package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.cors")
data class CorsEnvironment(
    // YAML 리스트 항목 하나에 쉼표로 넣으면 나뉘지 않으므로 쉼표로 구분한 단일 문자열로 받는다.
    // 쉼표 목록을 나눌 때 바인더가 항목마다 앞뒤 공백을 제거하므로 이 값은 이미 정규화되어 있다.
    val allowedOrigins: List<String>,
) {
    init {
        // 자격 증명을 허용하는 CORS에서 "*"는 요청 시점에야 예외가 나므로 기동 단계에서 막는다.
        require(allowedOrigins.isNotEmpty() && allowedOrigins.all { it.isNotBlank() && it.trim() != "*" }) {
            "spring.security.cors.allowed-origins must be a non-empty list of explicit origins"
        }
    }
}
