package team.themoment.datagsm.global.security.annotation

import org.springframework.security.access.prepost.PreAuthorize
import team.themoment.datagsm.domain.auth.entity.constant.ApiScope

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@scopeChecker.hasScope(authentication, '{scope.scope}')")
annotation class RequireScope(
    val scope: ApiScope,
)
