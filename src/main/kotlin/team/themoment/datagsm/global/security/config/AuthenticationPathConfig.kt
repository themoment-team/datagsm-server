package team.themoment.datagsm.global.security.config

object AuthenticationPathConfig {
    val PUBLIC_PATHS =
        listOf(
            "/v1/auth/google",
            "/v1/auth/refresh",
            "/v1/health",
            "/swagger-ui/**",
            "/api-docs/**",
        )

    val API_KEY_PATHS =
        listOf(
            "/v1/students",
        )

    val JWT_PATHS =
        listOf(
            "/v1/students/**",
            "/v1/auth/api-key",
        )
}