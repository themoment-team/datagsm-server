package team.themoment.datagsm.authorization.global.security.aspect

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import team.themoment.sdk.exception.ExpectedException
import team.themoment.datagsm.authorization.global.security.annotation.EmailRateLimited
import team.themoment.datagsm.authorization.global.security.service.EmailRateLimitService
import kotlin.reflect.full.memberProperties

@Aspect
@Component
class EmailRateLimitedAspect(
    private val emailRateLimitService: EmailRateLimitService,
) {
    @Before("@annotation(team.themoment.datagsm.global.security.annotation.EmailRateLimited)")
    fun checkEmailRateLimit(joinPoint: JoinPoint) {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val annotation = method.getAnnotation(EmailRateLimited::class.java)

        val email =
            extractEmailFromArgs(joinPoint.args)
                ?: throw ExpectedException("이메일 정보를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST)

        val result = emailRateLimitService.tryConsume(email, annotation.type)

        if (!result.consumed) {
            throw ExpectedException(
                "요청 횟수를 초과했습니다. ${result.secondsToWaitForRefill}초 후에 다시 시도해주세요.",
                HttpStatus.TOO_MANY_REQUESTS,
            )
        }
    }

    private fun extractEmailFromArgs(args: Array<Any>): String? {
        if (args.isEmpty()) return null

        val firstArg = args[0]

        // DTO 에서 email 필드 추출
        return try {
            firstArg::class
                .memberProperties
                .find { it.name == "email" }
                ?.getter
                ?.call(firstArg) as? String
        } catch (e: Exception) {
            null
        }
    }
}
