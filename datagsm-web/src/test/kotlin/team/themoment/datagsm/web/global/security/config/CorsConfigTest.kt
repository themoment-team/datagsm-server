package team.themoment.datagsm.web.global.security.config

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import team.themoment.datagsm.common.global.data.CorsEnvironment

class CorsConfigTest :
    DescribeSpec({
        fun corsConfiguration(properties: Map<String, String>): CorsConfiguration {
            val environment =
                Binder(MapConfigurationPropertySource(properties))
                    .bind("spring.security.cors", CorsEnvironment::class.java)
                    .get()
            val source = CorsConfig(environment).configure() as UrlBasedCorsConfigurationSource
            return requireNotNull(source.getCorsConfiguration(MockHttpServletRequest("GET", "/v1/accounts/my")))
        }

        describe("CorsConfig 클래스의") {
            describe("configure 메서드는") {
                context("쉼표 뒤에 공백이 있는 출처 목록이 설정되면") {
                    it("목록의 모든 출처를 자격 증명과 함께 허용한다") {
                        // Given
                        val configuration =
                            corsConfiguration(
                                mapOf("spring.security.cors.allowed-origins" to "http://localhost:3000, http://localhost:5173"),
                            )

                        // When & Then
                        configuration.allowCredentials shouldBe true
                        configuration.checkOrigin("http://localhost:3000") shouldBe "http://localhost:3000"
                        configuration.checkOrigin("http://localhost:5173") shouldBe "http://localhost:5173"
                        configuration.checkOrigin("https://evil.example.com").shouldBeNull()
                    }
                }

                context("YAML 리스트 형식으로 출처가 설정되면") {
                    it("항목마다 출처로 허용한다") {
                        // Given
                        val configuration =
                            corsConfiguration(
                                mapOf(
                                    "spring.security.cors.allowed-origins[0]" to "http://localhost:3000",
                                    "spring.security.cors.allowed-origins[1]" to "https://datagsm.kr",
                                ),
                            )

                        // When & Then
                        configuration.checkOrigin("http://localhost:3000") shouldBe "http://localhost:3000"
                        configuration.checkOrigin("https://datagsm.kr") shouldBe "https://datagsm.kr"
                    }
                }
            }
        }
    })
