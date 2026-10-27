package team.themoment.datagsm.web.global.security.jwt.filter

import io.jsonwebtoken.Jwts
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.web.global.data.OauthJwtVerificationEnvironment
import team.themoment.datagsm.web.global.security.jwt.JwtProvider
import tools.jackson.databind.ObjectMapper
import java.security.KeyPairGenerator
import java.util.Base64
import java.util.Date

class JwtAuthenticationFilterTest :
    DescribeSpec({

        val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val publicKeyPem = Base64.getEncoder().encodeToString(keyPair.public.encoded)
        val webClientId = "datagsm-web-client"
        val adminWebClientId = "datagsm-admin-web-client"

        val environment =
            OauthJwtVerificationEnvironment(
                publicKey = publicKeyPem,
                allowedClientIds = listOf(webClientId, adminWebClientId),
            )
        val filter = JwtAuthenticationFilter(JwtProvider(environment), ObjectMapper())

        fun accessToken(clientId: String?): String =
            Jwts
                .builder()
                .subject("admin@gsm.hs.kr")
                .claim("role", AccountRole.ADMIN.name)
                .apply { clientId?.let { claim("clientId", it) } }
                .issuedAt(Date())
                .expiration(Date(System.currentTimeMillis() + 60_000))
                .signWith(keyPair.private, Jwts.SIG.RS256)
                .compact()

        fun requestWith(token: String) =
            MockHttpServletRequest("GET", "/v1/accounts").apply {
                addHeader("Authorization", "Bearer $token")
            }

        afterEach {
            SecurityContextHolder.clearContext()
        }

        describe("JwtAuthenticationFilter 클래스는") {

            context("허용된 클라이언트로 발급된 토큰일 때") {
                it("토큰의 이메일과 역할로 인증하고 다음 필터로 넘겨야 한다") {
                    val request = requestWith(accessToken(webClientId))
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>(relaxed = true)

                    filter.doFilter(request, response, filterChain)

                    val authentication = SecurityContextHolder.getContext().authentication
                    authentication?.name shouldBe "admin@gsm.hs.kr"
                    authentication?.authorities?.map { it.authority } shouldBe listOf("ROLE_ADMIN")
                    verify(exactly = 1) { filterChain.doFilter(request, response) }
                }

                it("허용 목록의 다른 클라이언트도 인증해야 한다") {
                    val request = requestWith(accessToken(adminWebClientId))
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>(relaxed = true)

                    filter.doFilter(request, response, filterChain)

                    SecurityContextHolder.getContext().authentication?.name shouldBe "admin@gsm.hs.kr"
                    verify(exactly = 1) { filterChain.doFilter(request, response) }
                }
            }

            // 다른 서비스가 받은 토큰에도 사용자의 role이 들어 있어, 그대로 받으면 관리자 API가 열린다.
            context("다른 클라이언트로 발급된 관리자 토큰일 때") {
                it("401로 거절하고 인증을 만들지 않아야 한다") {
                    val request = requestWith(accessToken("third-party-client"))
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>(relaxed = true)

                    filter.doFilter(request, response, filterChain)

                    response.status shouldBe HttpStatus.UNAUTHORIZED.value()
                    response.contentAsString shouldContain "이 서비스에서 사용할 수 없는 토큰입니다."
                    SecurityContextHolder.getContext().authentication shouldBe null
                    verify(exactly = 0) { filterChain.doFilter(any(), any()) }
                }
            }

            context("clientId 클레임이 없는 토큰일 때") {
                it("401로 거절하고 인증을 만들지 않아야 한다") {
                    val request = requestWith(accessToken(null))
                    val response = MockHttpServletResponse()
                    val filterChain = mockk<FilterChain>(relaxed = true)

                    filter.doFilter(request, response, filterChain)

                    response.status shouldBe HttpStatus.UNAUTHORIZED.value()
                    SecurityContextHolder.getContext().authentication shouldBe null
                    verify(exactly = 0) { filterChain.doFilter(any(), any()) }
                }
            }
        }

        describe("OauthJwtVerificationEnvironment 클래스는") {
            // 빈 목록이면 모든 로그인이 조용히 막히므로, 설정 누락과 같이 기동 단계에서 실패시킨다.
            context("허용 클라이언트 목록이 비어 있을 때") {
                it("생성에 실패해야 한다") {
                    shouldThrow<IllegalArgumentException> {
                        OauthJwtVerificationEnvironment(publicKey = publicKeyPem, allowedClientIds = emptyList())
                    }
                }
            }
        }
    })
