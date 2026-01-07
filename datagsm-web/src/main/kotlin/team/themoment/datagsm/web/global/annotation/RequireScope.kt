package team.themoment.datagsm.global.security.annotation

import team.themoment.datagsm.common.domain.account.ApiScope

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireScope(
    val scope: ApiScope,
)
