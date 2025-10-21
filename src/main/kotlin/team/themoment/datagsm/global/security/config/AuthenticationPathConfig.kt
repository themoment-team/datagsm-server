package team.themoment.datagsm.global.security.config

object AuthenticationPathConfig {
    /**
     * 인증이 필요 없는 공개 경로
     */
    val PUBLIC_PATHS =
        listOf(
            "/v1/auth/google",
            "/v1/auth/refresh",
            "/v1/health",
            "/swagger-ui/**",
            "/api-docs/**",
        )

    /**
     * API Key 인증이 필요한 경로
     */
    val API_KEY_PATHS =
        listOf(
            "/v1/students",
        )

    /**
     * JWT 인증이 필요한 경로
     * PUBLIC_PATHS와 API_KEY_PATHS를 제외한 모든 경로는 JWT 인증이 적용됨
     */
    val JWT_PATHS =
        listOf(
            "/v1/students/**",
            "/v1/auth/api-key",
        )
}
