package team.themoment.datagsm.oauth.authorization.global.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.springframework.util.LinkedMultiValueMap
import team.themoment.datagsm.common.domain.oauth.dto.request.Oauth2TokenReqDto
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import java.util.Base64

class Oauth2TokenRequestReaderTest :
    DescribeSpec({
        fun basic(raw: String) = "Basic " + Base64.getEncoder().encodeToString(raw.toByteArray())

        describe("Oauth2TokenRequestReader 클래스의") {
            describe("fromForm 메서드는") {
                it("표준 파라미터 이름을 DTO로 옮기고 공백으로 구분된 scope를 나눠야 한다") {
                    // Given
                    val params =
                        LinkedMultiValueMap<String, String>().apply {
                            add("grant_type", "refresh_token")
                            add("client_id", "client-1")
                            add("client_secret", "secret")
                            add("refresh_token", "rt")
                            add("scope", "openid  app:student:read")
                        }

                    // When
                    val result = Oauth2TokenRequestReader.fromForm(params)

                    // Then
                    result shouldBe
                        Oauth2TokenReqDto(
                            grantType = "refresh_token",
                            clientId = "client-1",
                            clientSecret = "secret",
                            refreshToken = "rt",
                            scope = setOf("openid", "app:student:read"),
                        )
                }

                it("빈 scope는 생략한 것으로 보고 null로 두어야 한다") {
                    // Given
                    val params =
                        LinkedMultiValueMap<String, String>().apply {
                            add("grant_type", "refresh_token")
                            add("refresh_token", "rt")
                            add("scope", " ")
                        }

                    // When
                    val result = Oauth2TokenRequestReader.fromForm(params)

                    // Then
                    result.scope shouldBe null
                }

                it("grant_type이 없으면 invalid_request로 거절해야 한다") {
                    val params = LinkedMultiValueMap<String, String>().apply { add("client_id", "client-1") }

                    shouldThrow<OAuthException.InvalidRequest> { Oauth2TokenRequestReader.fromForm(params) }
                }

                it("같은 파라미터가 두 번 오면 invalid_request로 거절해야 한다") {
                    val params =
                        LinkedMultiValueMap<String, String>().apply {
                            add("grant_type", "authorization_code")
                            add("code", "a")
                            add("code", "b")
                        }

                    shouldThrow<OAuthException.InvalidRequest> { Oauth2TokenRequestReader.fromForm(params) }
                }
            }

            describe("applyBasicCredentials 메서드는") {
                val body = Oauth2TokenReqDto(grantType = "authorization_code", code = "code")

                it("Basic 헤더의 client_id와 client_secret을 URL 디코딩해 채워야 한다") {
                    // When
                    val result = Oauth2TokenRequestReader.applyBasicCredentials(body, basic("client%3A1:p%40ss%3Aword"))

                    // Then
                    result.clientId shouldBe "client:1"
                    result.clientSecret shouldBe "p@ss:word"
                }

                it("Basic이 아닌 Authorization 헤더는 무시해야 한다") {
                    Oauth2TokenRequestReader.applyBasicCredentials(body, "Bearer token") shouldBeSameInstanceAs body
                    Oauth2TokenRequestReader.applyBasicCredentials(body, null) shouldBeSameInstanceAs body
                }

                it("본문에도 client_secret이 있으면 두 방식을 함께 쓴 것으로 보고 거절해야 한다") {
                    shouldThrow<OAuthException.InvalidRequest> {
                        Oauth2TokenRequestReader.applyBasicCredentials(body.copy(clientSecret = "secret"), basic("client-1:secret"))
                    }
                }

                it("본문의 client_id가 헤더와 다르면 거절해야 한다") {
                    shouldThrow<OAuthException.InvalidRequest> {
                        Oauth2TokenRequestReader.applyBasicCredentials(body.copy(clientId = "other"), basic("client-1:secret"))
                    }
                }

                it("본문의 client_id가 헤더와 같으면 그대로 진행해야 한다") {
                    val result =
                        Oauth2TokenRequestReader.applyBasicCredentials(body.copy(clientId = "client-1"), basic("client-1:secret"))

                    result.clientSecret shouldBe "secret"
                }

                it("디코딩할 수 없는 헤더는 invalid_client로 거절해야 한다") {
                    shouldThrow<OAuthException.InvalidClient> {
                        Oauth2TokenRequestReader.applyBasicCredentials(body, "Basic !!!not-base64")
                    }
                    shouldThrow<OAuthException.InvalidClient> {
                        Oauth2TokenRequestReader.applyBasicCredentials(body, basic("no-separator"))
                    }
                }
            }
        }
    })
