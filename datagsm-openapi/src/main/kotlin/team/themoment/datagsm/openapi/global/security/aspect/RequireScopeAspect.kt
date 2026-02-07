package team.themoment.datagsm.openapi.global.security.aspect

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import team.themoment.datagsm.openapi.global.security.annotation.RequireScope
import team.themoment.datagsm.openapi.global.security.checker.ScopeChecker
import team.themoment.sdk.exception.ExpectedException

@Aspect
@Component
class RequireScopeAspect(
    private val scopeChecker: ScopeChecker,
) {
    @Before("@annotation(team.themoment.datagsm.openapi.global.security.annotation.RequireScope)")
    fun checkScope(joinPoint: JoinPoint) {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val requireScope = method.getAnnotation(RequireScope::class.java)

        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            throw ExpectedException("인증이 필요합니다", HttpStatus.UNAUTHORIZED)
        }

        val requiredScope = requireScope.scope.scope
        if (!scopeChecker.hasScope(authentication, requiredScope)) {
            throw ExpectedException("접근 권한이 부족합니다", HttpStatus.FORBIDDEN)
        }
    }
}
