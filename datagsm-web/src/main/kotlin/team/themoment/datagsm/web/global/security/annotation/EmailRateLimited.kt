package team.themoment.datagsm.web.global.security.annotation

/**
 * 이메일 기반 Rate Limit을 적용하는 어노테이션입니다.
 * 메서드의 첫 번째 파라미터에서 email 필드를 추출하여 rate limit 키로 사용합니다.
 * 메서드의 첫 번째 파라미터에서는 email 필드와 그것의 getter를 가지고 있어야 합니다.
 * @see team.themoment.datagsm.web.global.security.aspect.EmailRateLimitedAspect
 * @param type Rate Limit 타입 (SEND_EMAIL, CHECK_EMAIL)
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EmailRateLimited(
    val type: EmailRateLimitType,
)

enum class EmailRateLimitType {
    SEND_EMAIL,
    CHECK_EMAIL,
}
