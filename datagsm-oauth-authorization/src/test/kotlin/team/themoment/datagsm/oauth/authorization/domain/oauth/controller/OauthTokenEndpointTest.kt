package team.themoment.datagsm.oauth.authorization.domain.oauth.controller

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import team.themoment.datagsm.common.domain.oauth.dto.request.Oauth2TokenReqDto
import team.themoment.datagsm.common.domain.oauth.dto.response.Oauth2TokenResDto
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.Oauth2TokenService
import java.util.Base64

class OauthTokenEndpointTest :
    DescribeSpec({
        lateinit var oauth2TokenService: Oauth2TokenService
        lateinit var mockMvc: MockMvc
        lateinit var captured: io.mockk.CapturingSlot<Oauth2TokenReqDto>

        beforeEach {
            oauth2TokenService = mockk()
            captured = slot()
            every { oauth2TokenService.execute(capture(captured)) } returns
                Oauth2TokenResDto(accessToken = "at", expiresIn = 3600, refreshToken = "rt", scope = "openid")
            val controller =
                OauthController(
                    oauth2TokenService,
                    mockk(),
                    mockk(),
                    mockk(),
                    mockk(),
                    mockk(),
                    mockk(),
                    mockk(),
                    mockk(),
                    mockk(),
                    mockk(),
                )
            mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
        }

        describe("POST /v1/oauth/token은") {
            context("표준 라이브러리처럼 form 인코딩과 client_secret_basic으로 요청하면") {
                it("헤더의 클라이언트 인증 정보와 form 파라미터로 토큰을 발급해야 한다") {
                    // When
                    mockMvc
                        .post("/v1/oauth/token") {
                            contentType = MediaType.APPLICATION_FORM_URLENCODED
                            header(
                                HttpHeaders.AUTHORIZATION,
                                "Basic " + Base64.getEncoder().encodeToString("client-1:secret".toByteArray()),
                            )
                            param("grant_type", "authorization_code")
                            param("code", "code-1")
                            param("redirect_uri", "https://sp.example.com/callback")
                        }.andExpect {
                            status { isOk() }
                            header { string(HttpHeaders.CACHE_CONTROL, "no-store") }
                            header { string(HttpHeaders.PRAGMA, "no-cache") }
                        }

                    // Then
                    captured.captured shouldBe
                        Oauth2TokenReqDto(
                            grantType = "authorization_code",
                            clientId = "client-1",
                            clientSecret = "secret",
                            code = "code-1",
                            redirectUri = "https://sp.example.com/callback",
                        )
                }
            }

            context("기존처럼 JSON으로 요청하면") {
                it("배열 scope를 그대로 받고 캐시 금지 헤더를 붙여야 한다") {
                    mockMvc
                        .post("/v1/oauth/token") {
                            contentType = MediaType.APPLICATION_JSON
                            content =
                                """{"grant_type":"refresh_token","client_id":"client-1","client_secret":"secret","refresh_token":"rt","scope":["openid","app:student:read"]}"""
                        }.andExpect {
                            status { isOk() }
                            header { string(HttpHeaders.CACHE_CONTROL, "no-store") }
                        }

                    captured.captured.scope shouldBe setOf("openid", "app:student:read")
                    captured.captured.clientSecret shouldBe "secret"
                }

                it("표준 형식인 공백 구분 scope 문자열도 받아야 한다") {
                    mockMvc
                        .post("/v1/oauth/token") {
                            contentType = MediaType.APPLICATION_JSON
                            content =
                                """{"grant_type":"refresh_token","client_id":"client-1","refresh_token":"rt","scope":"openid app:student:read"}"""
                        }.andExpect { status { isOk() } }

                    captured.captured.scope shouldBe setOf("openid", "app:student:read")
                }

                it("빈 scope 문자열과 빈 배열은 생략한 것으로 보고 null로 받아야 한다") {
                    listOf("\"\"", "[]").forEach { emptyScope ->
                        mockMvc
                            .post("/v1/oauth/token") {
                                contentType = MediaType.APPLICATION_JSON
                                content =
                                    """{"grant_type":"refresh_token","client_id":"client-1","refresh_token":"rt","scope":$emptyScope}"""
                            }.andExpect { status { isOk() } }

                        captured.captured.scope shouldBe null
                    }
                }
            }
        }
    })
