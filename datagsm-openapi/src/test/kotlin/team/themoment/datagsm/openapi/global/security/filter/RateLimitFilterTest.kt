package team.themoment.datagsm.openapi.global.security.filter

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import team.themoment.datagsm.common.domain.auth.entity.ApiKey
import team.themoment.datagsm.common.global.dto.internal.RateLimitConsumeResult
import team.themoment.datagsm.openapi.global.security.authentication.ApiKeyAuthenticationToken
import team.themoment.datagsm.openapi.global.security.authentication.principal.ApiKeyPrincipal
import team.themoment.datagsm.openapi.global.security.service.RateLimitService
import tools.jackson.databind.ObjectMapper
import java.util.UUID

class RateLimitFilterTest :
    DescribeSpec({

        val mockRateLimitService = mockk<RateLimitService>()
        val filter = RateLimitFilter(mockRateLimitService, ObjectMapper())

        val apiKey =
            ApiKey().apply {
                id = 1L
                value = UUID.randomUUID()
                scopes = setOf("club:read")
                rateLimitCapacity = 100
                rateLimitRefillTokens = 100
                rateLimitRefillDurationSeconds = 60
            }

        beforeEach {
            SecurityContextHolder.getContext().authentication =
                ApiKeyAuthenticationToken(ApiKeyPrincipal("user@gsm.hs.kr", apiKey), emptySet())
        }

        afterEach {
            SecurityContextHolder.clearContext()
            clearAllMocks()
        }

        describe("RateLimitFilter 클래스의") {

            describe("doFilter 메서드는") {

                context("요청 제한을 초과했을 때") {
                    it("429를 UTF-8 JSON으로 반환해 한글 메시지가 깨지지 않아야 한다") {
                        val request = MockHttpServletRequest("GET", "/v1/clubs")
                        val response = MockHttpServletResponse()
                        val filterChain = mockk<FilterChain>(relaxed = true)

                        every { mockRateLimitService.tryConsumeAndReturnRemaining(apiKey) } returns
                            RateLimitConsumeResult(consumed = false, remainingTokens = 0, secondsToWaitForRefill = 30)

                        filter.doFilter(request, response, filterChain)

                        verify(exactly = 0) { filterChain.doFilter(any(), any()) }
                        response.status shouldBe HttpStatus.TOO_MANY_REQUESTS.value()
                        response.characterEncoding shouldBe "UTF-8"
                        response.contentType shouldBe "application/json;charset=UTF-8"
                        response.getHeader("Retry-After") shouldBe "30"
                        response.contentAsString shouldContain "API 요청 제한을 초과했습니다. 30초 후에 다시 시도해주세요."
                    }
                }

                context("요청 제한에 걸리지 않았을 때") {
                    it("다음 필터로 넘어가고 잔여 토큰 헤더가 설정되어야 한다") {
                        val request = MockHttpServletRequest("GET", "/v1/clubs")
                        val response = MockHttpServletResponse()
                        val filterChain = mockk<FilterChain>(relaxed = true)

                        every { mockRateLimitService.tryConsumeAndReturnRemaining(apiKey) } returns
                            RateLimitConsumeResult(consumed = true, remainingTokens = 99, secondsToWaitForRefill = 0)

                        filter.doFilter(request, response, filterChain)

                        verify(exactly = 1) { filterChain.doFilter(request, response) }
                        response.getHeader("X-RateLimit-Limit") shouldBe "100"
                        response.getHeader("X-RateLimit-Remaining") shouldBe "99"
                    }
                }
            }
        }
    })
