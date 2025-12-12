package team.themoment.datagsm.global.security.aspect

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import team.themoment.datagsm.global.security.annotation.RequireScope
import team.themoment.datagsm.global.security.checker.ScopeChecker

@Aspect
@Component
class RequireScopeAspect(
    private val scopeChecker: ScopeChecker,
) {
    @Before("@annotation(team.themoment.datagsm.global.security.annotation.RequireScope)")
    fun checkScope(joinPoint: JoinPoint) {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val requireScope = method.getAnnotation(RequireScope::class.java)

        val authentication =
            SecurityContextHolder.getContext().authentication
                ?: throw AccessDeniedException("인증이 필요합니다.")

        val requiredScope = requireScope.scope.scope
        if (!scopeChecker.hasScope(authentication, requiredScope)) {
            throw AccessDeniedException("접근 권한이 부족합니다")
        }
    }
}
