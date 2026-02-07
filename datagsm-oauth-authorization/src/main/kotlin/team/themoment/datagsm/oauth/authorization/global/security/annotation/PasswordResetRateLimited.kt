package team.themoment.datagsm.oauth.authorization.global.security.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PasswordResetRateLimited(
    val type: PasswordResetRateLimitType,
)

enum class PasswordResetRateLimitType(
    val bucketPrefix: String,
) {
    SEND_EMAIL("rate_limit:password_reset:send:"),
    CHECK_CODE("rate_limit:password_reset:check:"),
    MODIFY_PASSWORD("rate_limit:password_reset:modify:"),
    SIGNUP_SEND_EMAIL("rate_limit:signup:send:"),
    SIGNUP_CHECK_CODE("rate_limit:signup:check:"),
}
