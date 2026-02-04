package team.themoment.datagsm.oauth.authorization.global.security.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PasswordResetRateLimited(
    val type: PasswordResetRateLimitType,
)

enum class PasswordResetRateLimitType {
    SEND_EMAIL,
    CHECK_CODE,
    MODIFY_PASSWORD,
}
