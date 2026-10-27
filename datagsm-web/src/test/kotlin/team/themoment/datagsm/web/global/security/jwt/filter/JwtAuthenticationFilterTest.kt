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

        fun signedToken(
            subject: String? = "admin@gsm.hs.kr",
            clientId: String? = webClientId,
            role: String? = AccountRole.ADMIN.name,
            scopes: List<String>? = listOf("datagsm:account:read"),
        ): String =
            Jwts
                .builder()
                .apply { subject?.let { subject(it) } }
                .apply { role?.let { claim("role", it) } }
                .apply { clientId?.let { claim("clientId", it) } }
                .apply { scopes?.let { claim("scopes", it) } }
                .issuedAt(Date())
                .expiration(Date(System.currentTimeMillis() + 60_000))
                .signWith(keyPair.private, Jwts.SIG.RS256)
                .compact()

        fun accessToken(clientId: String?): String = signedToken(clientId = clientId)

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

        describe("JwtAuthenticationFilter 클래스는 토큰 종류와 클레임을") {
            fun assertRejected(token: String) {
                val request = requestWith(token)
                val response = MockHttpServletResponse()
                val filterChain = mockk<FilterChain>(relaxed = true)

                filter.doFilter(request, response, filterChain)

                response.status shouldBe HttpStatus.UNAUTHORIZED.value()
                SecurityContextHolder.getContext().authentication shouldBe null
                verify(exactly = 0) { filterChain.doFilter(any(), any()) }
            }

            // refresh token도 같은 clientId를 가지므로, role 유무가 아니라 access token에만 있는 scopes로 종류를 구분한다.
            context("허용된 클라이언트의 refresh token 형태(scopes 없음)일 때") {
                it("role이 있더라도 401로 거절해야 한다") {
                    assertRejected(signedToken(scopes = null))
                }

                it("role도 없는 실제 refresh token 형태도 401로 거절해야 한다") {
                    assertRejected(signedToken(role = null, scopes = null))
                }
            }

            context("sub 클레임이 없는 토큰일 때") {
                it("401로 거절해야 한다") {
                    assertRejected(signedToken(subject = null))
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

            // 꼬리 쉼표 같은 설정 오타가 빈 항목으로 조용히 섞이지 않게 한다.
            context("허용 클라이언트 목록에 빈 항목이 섞여 있을 때") {
                it("생성에 실패해야 한다") {
                    shouldThrow<IllegalArgumentException> {
                        OauthJwtVerificationEnvironment(publicKey = publicKeyPem, allowedClientIds = listOf(webClientId, " "))
                    }
                }
            }
        }
    })
