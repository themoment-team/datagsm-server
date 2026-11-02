package team.themoment.datagsm.common.global.data

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.boot.context.properties.bind.BindException
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource

class CorsEnvironmentTest :
    DescribeSpec({
        fun bind(allowedOrigins: String): CorsEnvironment =
            Binder(MapConfigurationPropertySource(mapOf("spring.security.cors.allowed-origins" to allowedOrigins)))
                .bind("spring.security.cors", CorsEnvironment::class.java)
                .get()

        describe("CorsEnvironment 클래스의") {
            context("쉼표로 구분한 origin 문자열이 주어지면") {
                it("바인딩 단계에서 각 origin의 앞뒤 공백을 제거해 나눈다") {
                    // When
                    val environment = bind("http://localhost:3000, https://datagsm.kr")

                    // Then
                    environment.allowedOrigins shouldBe listOf("http://localhost:3000", "https://datagsm.kr")
                }
            }

            context("와일드카드 origin이 포함되면") {
                it("자격 증명을 허용하는 CORS와 함께 쓸 수 없으므로 바인딩에 실패한다") {
                    // When
                    val exception = shouldThrow<BindException> { bind("http://localhost:3000, *") }

                    // Then
                    exception.rootCause().shouldBeInstanceOf<IllegalArgumentException>()
                }
            }

            context("와일드카드 패턴이나 경로가 포함된 origin이 주어지면") {
                it("allowedOrigins가 매칭하지 못하는 값이므로 바인딩에 실패한다") {
                    listOf("https://*.datagsm.kr", "http://localhost:*", "https://datagsm.kr/login", "datagsm.kr").forEach { origin ->
                        // When
                        val exception = shouldThrow<BindException> { bind(origin) }

                        // Then
                        exception.rootCause().shouldBeInstanceOf<IllegalArgumentException>()
                    }
                }
            }

            context("끝에 슬래시가 붙은 origin이 주어지면") {
                it("Spring이 슬래시를 제거하고 비교하므로 허용한다") {
                    // When
                    val environment = bind("https://datagsm.kr/")

                    // Then
                    environment.allowedOrigins shouldBe listOf("https://datagsm.kr/")
                }
            }

            context("빈 origin 항목이 포함되면") {
                it("바인딩에 실패한다") {
                    // When
                    val exception = shouldThrow<BindException> { bind("http://localhost:3000,,") }

                    // Then
                    exception.rootCause().shouldBeInstanceOf<IllegalArgumentException>()
                }
            }
        }
    })

private fun Throwable.rootCause(): Throwable = generateSequence(this) { it.cause }.last()
