package team.themoment.datagsm.web.global.security.config

object AuthenticationPathConfig {
    /**
     * 인증이 필요 없는 공개 경로
     */
    val PUBLIC_PATHS =
        listOf(
            "/v1/accounts/email/send",
            "/v1/accounts/email/check",
            "/v1/accounts/signup",
            "/v1/health",
            "/swagger-ui/**",
            "/v3/api-docs/**",
        )
}
