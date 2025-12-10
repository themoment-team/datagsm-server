package team.themoment.datagsm.global.security.annotation

import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@scopeChecker.hasScope(authentication, '{scope}')")
annotation class RequireScope(
    val scope: String,
)
