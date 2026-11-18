package team.themoment.datagsm.common.global.common.error

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import team.themoment.datagsm.common.global.common.discord.error.DiscordErrorNotificationService
import team.themoment.sdk.response.CommonApiResponse

private class SampleHandler {
    fun find(projectId: Long): Long = projectId
}

class GlobalExceptionHandlerTest :
    DescribeSpec({
        val discordErrorNotificationService = mockk<DiscordErrorNotificationService>(relaxed = true)
        val handler =
            GlobalExceptionHandler(
                discordErrorNotificationService = discordErrorNotificationService,
                environment = mockk(),
                errorMessageResolver = mockk(),
                templateEngine = mockk(),
            )

        fun typeMismatch(): MethodArgumentTypeMismatchException =
            MethodArgumentTypeMismatchException(
                "NOT_A_VALUE",
                Long::class.java,
                "projectId",
                MethodParameter(SampleHandler::class.java.getMethod("find", Long::class.javaPrimitiveType), 0),
                NumberFormatException("For input string"),
            )

        describe("GlobalExceptionHandler 클래스의") {
            describe("methodArgumentTypeMismatchException 메서드는") {
                context("경로 변수나 쿼리 값이 선언된 타입으로 변환되지 않으면") {
                    it("400과 형식 오류 메시지를 반환하고 장애 알림은 보내지 않는다") {
                        // When
                        val response = handler.methodArgumentTypeMismatchException(typeMismatch())

                        // Then
                        response.statusCode shouldBe HttpStatus.BAD_REQUEST
                        response.body shouldBe CommonApiResponse.error("요청 값의 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST)
                        verify(exactly = 0) { discordErrorNotificationService.notifyError(any(), any(), any()) }
                    }

                    it("Spring이 RuntimeException 핸들러보다 이 핸들러를 먼저 선택한다") {
                        // When
                        val resolved = ExceptionHandlerMethodResolver(GlobalExceptionHandler::class.java).resolveMethod(typeMismatch())

                        // Then
                        resolved?.name shouldBe "methodArgumentTypeMismatchException"
                    }
                }
            }
        }
    })
