package team.themoment.datagsm.oauth.authorization.global.security.jwt.filter

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import team.themoment.datagsm.oauth.authorization.global.security.jwt.JwtProvider
import tools.jackson.databind.json.JsonMapper

class JwtAuthenticationFilterTest :
    DescribeSpec({
        // Basic 헤더는 실제 JwtProvider도 Bearer 토큰으로 추출하지 못한다.
        val jwtProvider = mockk<JwtProvider> { every { extractToken(any()) } returns null }
        val filter = JwtAuthenticationFilter(jwtProvider, JsonMapper.builder().build())

        afterEach { SecurityContextHolder.clearContext() }

        fun request(path: String) =
            MockHttpServletRequest("POST", path).apply {
                addHeader(HttpHeaders.AUTHORIZATION, "Basic Y2xpZW50LTE6c2VjcmV0")
            }

        describe("JwtAuthenticationFilter 클래스는") {
            context("token 엔드포인트에 Basic 클라이언트 인증 헤더가 오면") {
                it("JWT로 해석하지 않고 컨트롤러까지 넘겨야 한다") {
                    // Given
                    val chain = MockFilterChain()
                    val response = MockHttpServletResponse()

                    // When
                    filter.doFilter(request("/v1/oauth/token"), response, chain)

                    // Then
                    chain.request.shouldNotBeNull()
                    response.status shouldBe 200
                }
            }

            context("다른 경로에 Bearer가 아닌 Authorization 헤더가 오면") {
                it("기존처럼 401로 거절해야 한다") {
                    // Given
                    val chain = MockFilterChain()
                    val response = MockHttpServletResponse()

                    // When
                    filter.doFilter(request("/v1/oauth/idp-sessions"), response, chain)

                    // Then
                    chain.request shouldBe null
                    response.status shouldBe 401
                }
            }
        }
    })
