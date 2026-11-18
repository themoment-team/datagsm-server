package team.themoment.datagsm.web.global.security.config

import io.jsonwebtoken.Jwts
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import jakarta.servlet.Filter
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockServletContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.support.GenericWebApplicationContext
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.global.data.PublicApiRateLimitEnvironment
import team.themoment.datagsm.web.global.data.OauthJwtVerificationEnvironment
import team.themoment.datagsm.web.global.security.handler.CustomAccessDeniedHandler
import team.themoment.datagsm.web.global.security.handler.CustomAuthenticationEntryPoint
import team.themoment.datagsm.web.global.security.jwt.JwtProvider
import team.themoment.datagsm.web.global.security.service.PublicApiRateLimitService
import tools.jackson.databind.ObjectMapper
import java.security.KeyPairGenerator
import java.util.Base64
import java.util.Date
import java.util.function.Supplier

class SecurityConfigTest :
    DescribeSpec({
        val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val webClientId = "datagsm-web-client"
        val jwtProvider =
            JwtProvider(
                OauthJwtVerificationEnvironment(
                    publicKey = Base64.getEncoder().encodeToString(keyPair.public.encoded),
                    allowedClientIds = listOf(webClientId),
                ),
            )

        val context =
            GenericWebApplicationContext(MockServletContext()).apply {
                registerBean("configure", CorsConfigurationSource::class.java, Supplier { UrlBasedCorsConfigurationSource() })
                registerBean(JwtProvider::class.java, Supplier { jwtProvider })
                registerBean(ObjectMapper::class.java, Supplier { ObjectMapper() })
                registerBean(CustomAuthenticationEntryPoint::class.java, Supplier { CustomAuthenticationEntryPoint(ObjectMapper()) })
                registerBean(CustomAccessDeniedHandler::class.java, Supplier { CustomAccessDeniedHandler(ObjectMapper()) })
                registerBean(PublicApiRateLimitService::class.java, Supplier { mockk<PublicApiRateLimitService>() })
                registerBean(PublicApiRateLimitEnvironment::class.java, Supplier { PublicApiRateLimitEnvironment() })
                AnnotatedBeanDefinitionReader(this).register(SecurityConfig::class.java)
                refresh()
            }
        val springSecurityFilterChain = context.getBean("springSecurityFilterChain", Filter::class.java)

        fun accessToken(role: AccountRole): String =
            Jwts
                .builder()
                .subject("user@gsm.hs.kr")
                .claim("role", role.name)
                .claim("clientId", webClientId)
                .claim("scopes", listOf("datagsm:account:read"))
                .issuedAt(Date())
                .expiration(Date(System.currentTimeMillis() + 60_000))
                .signWith(keyPair.private, Jwts.SIG.RS256)
                .compact()

        fun responseOf(
            method: String,
            path: String,
            role: AccountRole?,
        ): MockHttpServletResponse {
            val request =
                MockHttpServletRequest(method, path).apply {
                    role?.let { addHeader("Authorization", "Bearer ${accessToken(it)}") }
                }
            val response = MockHttpServletResponse()
            springSecurityFilterChain.doFilter(request, response, MockFilterChain())
            return response
        }

        afterEach {
            SecurityContextHolder.clearContext()
        }

        afterSpec {
            context.close()
        }

        describe("SecurityConfig 클래스의") {
            describe("securityFilterChain 메서드는") {
                listOf("/v1/accounts", "/v1/auth/api-keys", "/v1/students").forEach { path ->
                    context("일반 사용자가 관리자 전용 $path 경로를 조회하면") {
                        it("오류 재디스패치 없이 공통 응답으로 접근 권한 부족을 반환한다") {
                            // Given
                            val role = AccountRole.USER

                            // When
                            val response = responseOf("GET", path, role)

                            // Then
                            response.status shouldBe HttpStatus.FORBIDDEN.value()
                            response.errorMessage shouldBe null
                            response.contentType shouldBe "application/json;charset=UTF-8"
                            val body = ObjectMapper().readTree(response.contentAsString)
                            body["code"].asInt() shouldBe HttpStatus.FORBIDDEN.value()
                            body["message"].asText() shouldBe "접근 권한이 없습니다."
                        }
                    }
                }

                context("일반 사용자가 자신의 API 키를 삭제하면") {
                    it("관리자 전용 경로 변수 매처에 걸리지 않고 통과시킨다") {
                        responseOf("DELETE", "/v1/auth/api-keys/my", AccountRole.USER).status shouldBe HttpStatus.OK.value()
                    }
                }

                context("일반 사용자가 ID로 다른 API 키를 삭제하면") {
                    it("관리자 권한이 없으므로 거부한다") {
                        responseOf("DELETE", "/v1/auth/api-keys/1", AccountRole.USER).status shouldBe HttpStatus.FORBIDDEN.value()
                    }
                }

                context("관리자가 ID로 API 키를 삭제하면") {
                    it("통과시킨다") {
                        responseOf("DELETE", "/v1/auth/api-keys/1", AccountRole.ADMIN).status shouldBe HttpStatus.OK.value()
                    }
                }

                context("토큰 없이 자신의 API 키를 삭제하면") {
                    it("인증 실패로 거부한다") {
                        responseOf("DELETE", "/v1/auth/api-keys/my", null).status shouldBe HttpStatus.UNAUTHORIZED.value()
                    }
                }
            }
        }
    })
