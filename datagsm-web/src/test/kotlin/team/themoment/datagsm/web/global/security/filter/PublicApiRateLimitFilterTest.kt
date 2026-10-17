package team.themoment.datagsm.web.global.security.filter

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import team.themoment.datagsm.common.global.data.PublicApiRateLimitEnvironment
import team.themoment.datagsm.common.global.dto.internal.RateLimitConsumeResult
import team.themoment.datagsm.web.global.security.service.PublicApiRateLimitService
import tools.jackson.databind.ObjectMapper

class PublicApiRateLimitFilterTest :
    DescribeSpec({

        val mockRateLimitService = mockk<PublicApiRateLimitService>()
        val environment = PublicApiRateLimitEnvironment()
        val objectMapper = ObjectMapper()

        val filter = PublicApiRateLimitFilter(mockRateLimitService, environment, objectMapper)

        afterEach {
            clearAllMocks()
        }

        describe("PublicApiRateLimitFilter 클래스는") {

            context("공개 API 경로가 아닌 요청이 들어올 때") {
                it("요청 제한을 소비하지 않고 그대로 통과시켜야 한다") {
                    val request = MockHttpServletRequest("GET", "/v1/projects")
                    request.remoteAddr = "10.0.0.1"
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>(relaxed = true)

                    filter.doFilter(request, response, filterChain)

                    verify(exactly = 1) { filterChain.doFilter(request, response) }
                    verify(exactly = 0) { mockRateLimitService.tryConsumeAndReturnRemaining(any()) }
                    response.getHeader("X-RateLimit-Limit") shouldBe null
                }
            }

            context("요청 제한에 걸리지 않은 요청일 때") {
                it("다음 필터로 넘어가고 잔여 토큰 헤더가 설정되어야 한다") {
                    val request = MockHttpServletRequest("GET", "/v1/public/projects")
                    request.remoteAddr = "10.0.0.1"
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>(relaxed = true)

                    every { mockRateLimitService.tryConsumeAndReturnRemaining("10.0.0.1") } returns
                        RateLimitConsumeResult(consumed = true, remainingTokens = 59, secondsToWaitForRefill = 0)

                    filter.doFilter(request, response, filterChain)

                    verify(exactly = 1) { filterChain.doFilter(request, response) }
                    response.getHeader("X-RateLimit-Limit") shouldBe "60"
                    response.getHeader("X-RateLimit-Remaining") shouldBe "59"
                }
            }

            context("요청 제한을 초과했을 때") {
                it("429와 Retry-After를 반환하고 다음 필터로 넘어가지 않아야 한다") {
                    val request = MockHttpServletRequest("GET", "/v1/public/projects")
                    request.remoteAddr = "10.0.0.1"
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>(relaxed = true)

                    every { mockRateLimitService.tryConsumeAndReturnRemaining("10.0.0.1") } returns
                        RateLimitConsumeResult(consumed = false, remainingTokens = 0, secondsToWaitForRefill = 30)

                    filter.doFilter(request, response, filterChain)

                    verify(exactly = 0) { filterChain.doFilter(any(), any()) }
                    response.status shouldBe HttpStatus.TOO_MANY_REQUESTS.value()
                    response.getHeader("Retry-After") shouldBe "30"
                }
            }

            context("X-Forwarded-For 헤더가 있을 때") {
                it("클라이언트가 위조할 수 없는 마지막 항목을 키로 사용해야 한다") {
                    val request = MockHttpServletRequest("GET", "/v1/public/projects")
                    request.remoteAddr = "10.0.0.1"
                    request.addHeader("X-Forwarded-For", "1.2.3.4, 203.0.113.9")
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>(relaxed = true)

                    val capturedIp = slot<String>()
                    every { mockRateLimitService.tryConsumeAndReturnRemaining(capture(capturedIp)) } returns
                        RateLimitConsumeResult(consumed = true, remainingTokens = 59, secondsToWaitForRefill = 0)

                    filter.doFilter(request, response, filterChain)

                    capturedIp.captured shouldBe "203.0.113.9"
                }
            }

            context("X-Forwarded-For 헤더가 없을 때") {
                it("remoteAddr를 키로 사용해야 한다") {
                    val request = MockHttpServletRequest("GET", "/v1/public/projects")
                    request.remoteAddr = "198.51.100.7"
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>(relaxed = true)

                    val capturedIp = slot<String>()
                    every { mockRateLimitService.tryConsumeAndReturnRemaining(capture(capturedIp)) } returns
                        RateLimitConsumeResult(consumed = true, remainingTokens = 59, secondsToWaitForRefill = 0)

                    filter.doFilter(request, response, filterChain)

                    capturedIp.captured shouldBe "198.51.100.7"
                }
            }
        }
    })
