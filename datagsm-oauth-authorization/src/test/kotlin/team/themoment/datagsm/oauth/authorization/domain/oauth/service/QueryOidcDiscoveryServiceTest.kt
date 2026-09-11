package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.QueryOidcDiscoveryServiceImpl
import team.themoment.datagsm.oauth.authorization.global.data.OauthJwtProvisionEnvironment

class QueryOidcDiscoveryServiceTest :
    DescribeSpec({

        val mockOauthEnvironment = mockk<OauthEnvironment>()
        val mockJwtEnvironment = mockk<OauthJwtProvisionEnvironment>()

        val queryOidcDiscoveryService =
            QueryOidcDiscoveryServiceImpl(mockOauthEnvironment, mockJwtEnvironment)

        afterEach {
            clearAllMocks()
        }

        describe("QueryOidcDiscoveryService 클래스의") {
            describe("execute 메서드는") {

                beforeEach {
                    every { mockOauthEnvironment.issuerUrl } returns "https://oauth.authorization.datagsm.kr"
                    every { mockOauthEnvironment.userinfoUrl } returns "https://oauth.userinfo.datagsm.kr/userinfo"
                    every { mockJwtEnvironment.datagsmApplicationId } returns "datagsm"
                }

                context("Discovery 문서를 요청했을 때") {
                    it("issuer 기준으로 엔드포인트가 조립되어야 한다") {
                        val result = queryOidcDiscoveryService.execute()

                        result.issuer shouldBe "https://oauth.authorization.datagsm.kr"
                        result.authorizationEndpoint shouldBe "https://oauth.authorization.datagsm.kr/v1/oauth/authorize"
                        result.tokenEndpoint shouldBe "https://oauth.authorization.datagsm.kr/v1/oauth/token"
                        result.jwksUri shouldBe "https://oauth.authorization.datagsm.kr/v1/oauth/jwks"
                        result.endSessionEndpoint shouldBe "https://oauth.authorization.datagsm.kr/v1/oauth/logout"
                    }

                    it("UserInfo는 별도 호스트 설정값을 그대로 써야 한다") {
                        val result = queryOidcDiscoveryService.execute()

                        result.userinfoEndpoint shouldBe "https://oauth.userinfo.datagsm.kr/userinfo"
                    }

                    // 목록을 손으로 적으면 구현이 바뀌어도 문서만 남아 SP가 지원하지 않는 값을 쓰게 된다.
                    it("지원 grant_type이 실제 구현과 일치해야 한다") {
                        val result = queryOidcDiscoveryService.execute()

                        result.grantTypesSupported shouldBe
                            listOf("authorization_code", "refresh_token", "client_credentials")
                    }

                    it("지원 PKCE 방식이 실제 구현과 일치해야 한다") {
                        val result = queryOidcDiscoveryService.execute()

                        result.codeChallengeMethodsSupported.toSet() shouldBe setOf("plain", "S256")
                    }

                    it("scope 목록에 openid와 애플리케이션 scope가 포함되어야 한다") {
                        val result = queryOidcDiscoveryService.execute()

                        result.scopesSupported.contains("openid") shouldBe true
                        result.scopesSupported.contains("datagsm:account_read") shouldBe true
                        result.scopesSupported.contains("datagsm:student_read") shouldBe true
                    }

                    it("id_token 서명 알고리즘은 실제 서명과 같은 RS256이어야 한다") {
                        val result = queryOidcDiscoveryService.execute()

                        result.idTokenSigningAlgValuesSupported shouldBe listOf("RS256")
                    }
                }

                context("issuer_url 끝에 슬래시가 있을 때") {
                    it("엔드포인트에 슬래시가 중복되지 않아야 한다") {
                        every { mockOauthEnvironment.issuerUrl } returns "https://oauth.authorization.datagsm.kr/"

                        val result = queryOidcDiscoveryService.execute()

                        result.issuer shouldBe "https://oauth.authorization.datagsm.kr"
                        result.tokenEndpoint shouldBe "https://oauth.authorization.datagsm.kr/v1/oauth/token"
                    }
                }
            }
        }
    })
