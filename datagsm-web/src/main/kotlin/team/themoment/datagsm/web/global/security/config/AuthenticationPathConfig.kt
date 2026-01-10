package team.themoment.datagsm.web.global.security.config

object AuthenticationPathConfig {
    /**
     * 인증이 필요 없는 공개 경로
     */
    val PUBLIC_PATHS =
        listOf(
            "/v1/auth/login",
            "/v1/auth/refresh",
            "/v1/auth/google",
            "/v1/oauth/code",
            "/v1/oauth/token",
            "/v1/oauth/refresh",
            "/v1/account/email/send",
            "/v1/account/email/check",
            "/v1/account/signup",
            "/v1/health",
            "/swagger-ui/**",
            "/v3/api-docs/**",
        )
}
