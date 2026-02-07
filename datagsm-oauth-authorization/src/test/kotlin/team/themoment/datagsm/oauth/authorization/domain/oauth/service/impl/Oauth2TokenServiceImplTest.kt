package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.dto.request.Oauth2TokenReqDto
import team.themoment.datagsm.common.domain.oauth.entity.OauthCodeRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.OauthRefreshTokenRedisEntity
import team.themoment.datagsm.common.domain.oauth.repository.OauthCodeRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthRefreshTokenRedisRepository
import team.themoment.datagsm.common.global.data.OauthJwtEnvironment
import team.themoment.datagsm.oauth.authorization.global.security.jwt.JwtProvider
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class Oauth2TokenServiceImplTest :
    DescribeSpec({

        val mockOauthCodeRedisRepository = mockk<OauthCodeRedisRepository>()
        val mockOauthRefreshTokenRedisRepository = mockk<OauthRefreshTokenRedisRepository>()
        val mockClientJpaRepository = mockk<ClientJpaRepository>()
        val mockAccountJpaRepository = mockk<AccountJpaRepository>()
        val mockPasswordEncoder = mockk<PasswordEncoder>()
        val mockJwtProvider = mockk<JwtProvider>()
        val mockJwtEnvironment = mockk<OauthJwtEnvironment>()

        val service =
            Oauth2TokenServiceImpl(
                mockOauthCodeRedisRepository,
                mockOauthRefreshTokenRedisRepository,
                mockClientJpaRepository,
                mockAccountJpaRepository,
                mockPasswordEncoder,
                mockJwtProvider,
                mockJwtEnvironment,
            )

        afterEach {
            clearAllMocks()
        }

        describe("Oauth2TokenServiceImpl 클래스의") {
            describe("execute 메서드는") {

                context("grant_type=authorization_code로 토큰을 요청할 때") {
                    val reqDto =
                        Oauth2TokenReqDto(
                            grantType = "authorization_code",
                            code = "test-code",
                            clientId = "test-client",
                            clientSecret = "test-secret",
                            redirectUri = "https://example.com/callback",
                        )

                    val code =
                        OauthCodeRedisEntity(
                            email = "test@gsm.hs.kr",
                            clientId = "test-client",
                            codeChallenge = null,
                            codeChallengeMethod = null,
                            code = "test-code",
                            ttl = 300,
                        )

                    val client =
                        ClientJpaEntity().apply {
                            id = "test-client"
                            secret = "hashed-secret"
                            redirectUrls = setOf("https://example.com/callback")
                            scopes = setOf("self:read")
                        }

                    val account =
                        AccountJpaEntity().apply {
                            email = "test@gsm.hs.kr"
                            role = AccountRole.USER
                        }

                    beforeEach {
                        every { mockOauthCodeRedisRepository.findById("test-code") } returns Optional.of(code)
                        every { mockClientJpaRepository.findById("test-client") } returns Optional.of(client)
                        every { mockPasswordEncoder.matches("test-secret", "hashed-secret") } returns true
                        every { mockAccountJpaRepository.findByEmail("test@gsm.hs.kr") } returns Optional.of(account)
                        every { mockJwtProvider.generateOauthAccessToken(any(), any(), any(), any()) } returns "access-token"
                        every { mockJwtProvider.generateOauthRefreshToken(any(), any()) } returns "refresh-token"
                        every { mockJwtEnvironment.accessTokenExpiration } returns 3600000L
                        every { mockJwtEnvironment.refreshTokenExpiration } returns 2592000000L
                        every { mockOauthRefreshTokenRedisRepository.deleteByEmailAndClientId(any(), any()) } returns Unit
                        every { mockOauthRefreshTokenRedisRepository.save(any()) } answers { firstArg() }
                        every { mockOauthCodeRedisRepository.delete(any()) } returns Unit
                    }

                    it("표준 응답 형식으로 토큰이 반환된다") {
                        val result = service.execute(reqDto)

                        result.accessToken shouldBe "access-token"
                        result.tokenType shouldBe "Bearer"
                        result.expiresIn shouldBe 3600L
                        result.refreshToken shouldBe "refresh-token"
                        result.scope shouldNotBe null

                        verify(exactly = 1) { mockOauthCodeRedisRepository.findById("test-code") }
                        verify(exactly = 1) { mockOauthCodeRedisRepository.delete(code) }
                    }
                }

                context("PKCE를 사용하는 authorization_code 요청일 때") {
                    val reqDto =
                        Oauth2TokenReqDto(
                            grantType = "authorization_code",
                            code = "test-code",
                            clientId = "test-client",
                            clientSecret = "test-secret",
                            codeVerifier = "wrong-verifier",
                        )

                    val code =
                        OauthCodeRedisEntity(
                            email = "test@gsm.hs.kr",
                            clientId = "test-client",
                            codeChallenge = "challenge-hash",
                            codeChallengeMethod = "S256",
                            code = "test-code",
                            ttl = 300,
                        )

                    val client =
                        ClientJpaEntity().apply {
                            id = "test-client"
                            secret = "hashed-secret"
                        }

                    beforeEach {
                        every { mockOauthCodeRedisRepository.findById("test-code") } returns Optional.of(code)
                        every { mockClientJpaRepository.findById("test-client") } returns Optional.of(client)
                        every { mockPasswordEncoder.matches(any(), any()) } returns true
                    }

                    it("잘못된 code_verifier로 요청하면 400 에러가 발생한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(reqDto)
                            }
                        exception.message shouldBe "code_verifier가 일치하지 않습니다."
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("grant_type=refresh_token으로 토큰을 요청할 때") {
                    val reqDto =
                        Oauth2TokenReqDto(
                            grantType = "refresh_token",
                            refreshToken = "valid-refresh-token",
                            clientId = "test-client",
                            clientSecret = "test-secret",
                        )

                    val client =
                        ClientJpaEntity().apply {
                            id = "test-client"
                            secret = "hashed-secret"
                            scopes = setOf("self:read")
                        }

                    val account =
                        AccountJpaEntity().apply {
                            email = "test@gsm.hs.kr"
                            role = AccountRole.USER
                        }

                    val storedToken =
                        OauthRefreshTokenRedisEntity(
                            id = "test@gsm.hs.kr:test-client",
                            email = "test@gsm.hs.kr",
                            clientId = "test-client",
                            token = "valid-refresh-token",
                            ttl = 2592000L,
                        )

                    beforeEach {
                        every { mockJwtProvider.validateToken("valid-refresh-token") } returns true
                        every { mockJwtProvider.getEmailFromToken("valid-refresh-token") } returns "test@gsm.hs.kr"
                        every { mockJwtProvider.getClientIdFromToken("valid-refresh-token") } returns "test-client"
                        every { mockClientJpaRepository.findById("test-client") } returns Optional.of(client)
                        every { mockPasswordEncoder.matches("test-secret", "hashed-secret") } returns true
                        every { mockOauthRefreshTokenRedisRepository.findByEmailAndClientId("test@gsm.hs.kr", "test-client") } returns
                            Optional.of(storedToken)
                        every { mockAccountJpaRepository.findByEmail("test@gsm.hs.kr") } returns Optional.of(account)
                        every { mockJwtProvider.generateOauthAccessToken(any(), any(), any(), any()) } returns "new-access-token"
                        every { mockJwtProvider.generateOauthRefreshToken(any(), any()) } returns "new-refresh-token"
                        every { mockJwtEnvironment.accessTokenExpiration } returns 3600000L
                        every { mockJwtEnvironment.refreshTokenExpiration } returns 2592000000L
                        every { mockOauthRefreshTokenRedisRepository.deleteByEmailAndClientId(any(), any()) } returns Unit
                        every { mockOauthRefreshTokenRedisRepository.save(any()) } answers { firstArg() }
                    }

                    it("새로운 토큰이 발급된다") {
                        val result = service.execute(reqDto)

                        result.accessToken shouldBe "new-access-token"
                        result.tokenType shouldBe "Bearer"
                        result.expiresIn shouldBe 3600L
                        result.refreshToken shouldBe "new-refresh-token"

                        verify(exactly = 1) { mockJwtProvider.validateToken("valid-refresh-token") }
                        verify(exactly = 1) { mockOauthRefreshTokenRedisRepository.save(any()) }
                    }
                }

                context("grant_type=client_credentials로 토큰을 요청할 때") {
                    val reqDto =
                        Oauth2TokenReqDto(
                            grantType = "client_credentials",
                            clientId = "test-client",
                            clientSecret = "test-secret",
                            scope = "self:read",
                        )

                    val client =
                        ClientJpaEntity().apply {
                            id = "test-client"
                            secret = "hashed-secret"
                            scopes = setOf("self:read")
                        }

                    beforeEach {
                        every { mockClientJpaRepository.findById("test-client") } returns Optional.of(client)
                        every { mockPasswordEncoder.matches("test-secret", "hashed-secret") } returns true
                        every { mockJwtProvider.generateClientCredentialsAccessToken(any(), any()) } returns "client-access-token"
                        every { mockJwtEnvironment.accessTokenExpiration } returns 3600000L
                    }

                    it("refresh_token 없이 access_token만 발급된다") {
                        val result = service.execute(reqDto)

                        result.accessToken shouldBe "client-access-token"
                        result.tokenType shouldBe "Bearer"
                        result.expiresIn shouldBe 3600L
                        result.refreshToken shouldBe null

                        verify(
                            exactly = 1,
                        ) { mockJwtProvider.generateClientCredentialsAccessToken("test-client", setOf(OAuthScope.SELF_READ)) }
                        verify(exactly = 0) { mockJwtProvider.generateOauthRefreshToken(any(), any()) }
                    }
                }

                context("지원하지 않는 grant_type으로 요청하면") {
                    val reqDto = Oauth2TokenReqDto(grantType = "password")

                    it("IllegalArgumentException이 발생한다") {
                        shouldThrow<IllegalArgumentException> {
                            service.execute(reqDto)
                        }
                    }
                }

                context("필수 파라미터가 누락되면") {
                    val reqDto =
                        Oauth2TokenReqDto(
                            grantType = "authorization_code",
                            code = null,
                            clientId = "test-client",
                            clientSecret = "test-secret",
                        )

                    it("ExpectedException이 발생한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(reqDto)
                            }
                        exception.message shouldBe "code는 필수입니다."
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }
            }
        }
    })
