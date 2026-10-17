package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.LogoutIdpSessionServiceImpl

class LogoutIdpSessionServiceTest :
    DescribeSpec({

        val mockIdpSessionRedisRepository = mockk<IdpSessionRedisRepository>(relaxed = true)
        val mockOauthEnvironment = mockk<OauthEnvironment>()

        val logoutIdpSessionService =
            LogoutIdpSessionServiceImpl(
                mockIdpSessionRedisRepository,
                mockOauthEnvironment,
            )

        afterEach {
            clearAllMocks()
        }

        describe("LogoutIdpSessionService 클래스의") {
            describe("execute 메서드는") {

                val testSessionId = "session-1"

                beforeEach {
                    every { mockOauthEnvironment.idpSessionCookieName } returns "datagsm_idp_session"
                    every { mockOauthEnvironment.idpSessionCookieSecure } returns true
                    every { mockOauthEnvironment.idpSessionExpirationSeconds } returns 28800L
                }

                context("유효한 세션 쿠키가 주어졌을 때") {
                    it("세션을 삭제하고 204를 반환해야 한다") {
                        val response = logoutIdpSessionService.execute(testSessionId)

                        response.statusCode shouldBe HttpStatus.NO_CONTENT
                        verify(exactly = 1) { mockIdpSessionRedisRepository.deleteById(testSessionId) }
                    }

                    it("쿠키를 즉시 만료시켜야 한다") {
                        val response = logoutIdpSessionService.execute(testSessionId)

                        val setCookie = response.headers.getFirst(HttpHeaders.SET_COOKIE) ?: ""
                        setCookie shouldContain "datagsm_idp_session="
                        setCookie shouldContain "Max-Age=0"
                    }

                    // 브라우저는 name/path/domain이 모두 일치해야 기존 쿠키를 덮어쓴다.
                    // 발급 시 속성과 어긋나면 로그아웃이 조용히 실패한다.
                    it("발급 시와 같은 속성으로 쿠키를 덮어써야 한다") {
                        val response = logoutIdpSessionService.execute(testSessionId)

                        val setCookie = response.headers.getFirst(HttpHeaders.SET_COOKIE) ?: ""
                        // "Path=/" 부분 일치로 검사하면 Path=/v1 같은 불일치도 통과하므로
                        // 속성 단위로 잘라 정확히 비교한다.
                        val attributes = setCookie.split("; ").map { it.trim() }
                        attributes.contains("Path=/") shouldBe true
                        setCookie shouldContain "HttpOnly"
                        setCookie shouldContain "Secure"
                        setCookie shouldContain "SameSite=Lax"
                    }
                }

                context("세션 쿠키가 없을 때") {
                    it("세션 존재 여부를 노출하지 않도록 동일하게 204를 반환해야 한다") {
                        val response = logoutIdpSessionService.execute(null)

                        response.statusCode shouldBe HttpStatus.NO_CONTENT
                        verify(exactly = 0) { mockIdpSessionRedisRepository.deleteById(any()) }
                    }

                    it("잔여 쿠키가 남지 않도록 만료 쿠키는 내려줘야 한다") {
                        val response = logoutIdpSessionService.execute(null)

                        (response.headers.getFirst(HttpHeaders.SET_COOKIE) ?: "") shouldContain "Max-Age=0"
                    }
                }

                context("빈 문자열 쿠키가 주어졌을 때") {
                    it("삭제를 시도하지 않아야 한다") {
                        logoutIdpSessionService.execute("")

                        verify(exactly = 0) { mockIdpSessionRedisRepository.deleteById(any()) }
                    }
                }

                context("HTTP 로컬 환경일 때") {
                    it("Secure 속성 없이 쿠키를 만료시켜야 한다") {
                        every { mockOauthEnvironment.idpSessionCookieSecure } returns false

                        val response = logoutIdpSessionService.execute(testSessionId)

                        (response.headers.getFirst(HttpHeaders.SET_COOKIE) ?: "").contains("Secure") shouldBe false
                    }
                }
            }
        }
    })
