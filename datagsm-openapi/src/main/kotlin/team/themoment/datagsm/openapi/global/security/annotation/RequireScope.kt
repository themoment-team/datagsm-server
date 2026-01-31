package team.themoment.datagsm.resource.global.security.annotation

import team.themoment.datagsm.common.domain.auth.entity.constant.ApiKeyScope

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireScope(
    val scope: ApiKeyScope,
)
