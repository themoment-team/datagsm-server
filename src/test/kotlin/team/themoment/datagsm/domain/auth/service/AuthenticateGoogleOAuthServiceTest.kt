package team.themoment.datagsm.domain.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.domain.auth.repository.RefreshTokenRedisRepository
import team.themoment.datagsm.domain.auth.service.impl.AuthenticateGoogleOAuthServiceImpl
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.global.security.jwt.JwtProperties
import team.themoment.datagsm.global.security.jwt.JwtProvider
import team.themoment.datagsm.global.thirdparty.feign.oauth.GoogleOAuth2Client
import team.themoment.datagsm.global.thirdparty.feign.oauth.GoogleUserInfoClient
import team.themoment.datagsm.global.thirdparty.feign.oauth.dto.GoogleTokenResDto
import team.themoment.datagsm.global.thirdparty.feign.oauth.dto.GoogleUserInfoResDto
import java.util.Optional

class AuthenticateGoogleOAuthServiceTest :
    DescribeSpec({

        val mockClientRegistrationRepository = mockk<ClientRegistrationRepository>()
        val mockGoogleOAuth2Client = mockk<GoogleOAuth2Client>()
        val mockGoogleUserInfoClient = mockk<GoogleUserInfoClient>()
        val mockAccountRepository = mockk<AccountJpaRepository>()
        val mockStudentRepository = mockk<StudentJpaRepository>()
        val mockJwtProvider = mockk<JwtProvider>()
        val mockJwtProperties = mockk<JwtProperties>()
        val mockRefreshTokenRepository = mockk<RefreshTokenRedisRepository>()

        val authenticateGoogleOAuthService =
            AuthenticateGoogleOAuthServiceImpl(
                mockClientRegistrationRepository,
                mockGoogleOAuth2Client,
                mockGoogleUserInfoClient,
                mockAccountRepository,
                mockStudentRepository,
                mockJwtProvider,
                mockJwtProperties,
                mockRefreshTokenRepository,
            )

        afterEach {
            clearAllMocks()
        }

        describe("AuthenticateGoogleOAuthService 클래스의") {
            describe("execute 메서드는") {

                val authCode = "test_auth_code"
                val email = "test@gsm.hs.kr"
                val accessToken = "google.access.token"
                val jwtAccessToken = "jwt.access.token"
                val jwtRefreshToken = "jwt.refresh.token"

                val clientRegistration =
                    ClientRegistration
                        .withRegistrationId("google")
                        .clientId("test-client-id")
                        .clientSecret("test-client-secret")
                        .redirectUri("http://localhost:8080/callback")
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                        .tokenUri("https://oauth2.googleapis.com/token")
                        .build()

                beforeEach {
                    every { mockJwtProperties.refreshTokenExpiration } returns 604800000L
                }

                context("Google OAuth 설정을 찾을 수 없을 때") {
                    beforeEach {
                        every { mockClientRegistrationRepository.findByRegistrationId("google") } returns null
                    }

                    it("IllegalArgumentException이 발생해야 한다") {
                        val exception =
                            shouldThrow<IllegalArgumentException> {
                                authenticateGoogleOAuthService.execute(authCode)
                            }

                        exception.message shouldBe "Google OAuth 설정을 찾을 수 없습니다."

                        verify(exactly = 1) { mockClientRegistrationRepository.findByRegistrationId("google") }
                        verify(exactly = 0) { mockGoogleOAuth2Client.exchangeCodeForToken(any()) }
                    }
                }

                context("신규 사용자가 OAuth 인증을 시도할 때") {
                    val tokenResponse =
                        GoogleTokenResDto(
                            accessToken = accessToken,
                            expiresIn = 3600,
                            scope = "email profile",
                            tokenType = "Bearer",
                        )

                    val userInfo =
                        GoogleUserInfoResDto(
                            id = "123456",
                            email = email,
                            verifiedEmail = true,
                            name = "Test User",
                            picture = "https://example.com/picture.jpg",
                        )

                    val newAccount =
                        AccountJpaEntity.create(email).apply {
                            id = 1L
                        }

                    beforeEach {
                        every { mockClientRegistrationRepository.findByRegistrationId("google") } returns
                            clientRegistration
                        every { mockGoogleOAuth2Client.exchangeCodeForToken(any()) } returns tokenResponse
                        every { mockGoogleUserInfoClient.getUserInfo("Bearer $accessToken") } returns userInfo
                        every { mockAccountRepository.findByEmail(email) } returns Optional.empty()
                        every { mockStudentRepository.findByEmail(email) } returns Optional.empty()
                        every { mockAccountRepository.save(any()) } returns newAccount
                        every { mockJwtProvider.generateAccessToken(email, AccountRole.USER) } returns
                            jwtAccessToken
                        every { mockJwtProvider.generateRefreshToken(email) } returns jwtRefreshToken
                        every { mockRefreshTokenRepository.deleteByEmail(email) } returns Unit
                        every { mockRefreshTokenRepository.save(any()) } returns mockk()
                    }

                    it("새로운 계정을 생성하고 토큰을 발급해야 한다") {
                        val result = authenticateGoogleOAuthService.execute(authCode)

                        result.accessToken shouldBe jwtAccessToken
                        result.refreshToken shouldBe jwtRefreshToken

                        verify(exactly = 1) { mockAccountRepository.save(any()) }
                        verify(exactly = 1) { mockJwtProvider.generateAccessToken(email, AccountRole.USER) }
                        verify(exactly = 1) { mockJwtProvider.generateRefreshToken(email) }
                        verify(exactly = 1) { mockRefreshTokenRepository.save(any()) }
                    }
                }

                context("기존 계정이 있는 사용자가 OAuth 인증을 시도할 때") {
                    val tokenResponse =
                        GoogleTokenResDto(
                            accessToken = accessToken,
                            expiresIn = 3600,
                            scope = "email profile",
                            tokenType = "Bearer",
                        )

                    val userInfo =
                        GoogleUserInfoResDto(
                            id = "123456",
                            email = email,
                            verifiedEmail = true,
                            name = "Existing User",
                            picture = "https://example.com/picture.jpg",
                        )

                    val existingAccount =
                        AccountJpaEntity.create(email).apply {
                            id = 1L
                        }

                    beforeEach {
                        every { mockClientRegistrationRepository.findByRegistrationId("google") } returns
                            clientRegistration
                        every { mockGoogleOAuth2Client.exchangeCodeForToken(any()) } returns tokenResponse
                        every { mockGoogleUserInfoClient.getUserInfo("Bearer $accessToken") } returns userInfo
                        every { mockAccountRepository.findByEmail(email) } returns
                            Optional.of(existingAccount)
                        every { mockJwtProvider.generateAccessToken(email, AccountRole.USER) } returns
                            jwtAccessToken
                        every { mockJwtProvider.generateRefreshToken(email) } returns jwtRefreshToken
                        every { mockRefreshTokenRepository.deleteByEmail(email) } returns Unit
                        every { mockRefreshTokenRepository.save(any()) } returns mockk()
                    }

                    it("기존 계정으로 토큰을 발급해야 한다") {
                        val result = authenticateGoogleOAuthService.execute(authCode)

                        result.accessToken shouldBe jwtAccessToken
                        result.refreshToken shouldBe jwtRefreshToken

                        verify(exactly = 0) { mockAccountRepository.save(any()) }
                        verify(exactly = 1) { mockJwtProvider.generateAccessToken(email, AccountRole.USER) }
                        verify(exactly = 1) { mockRefreshTokenRepository.save(any()) }
                    }
                }

                context("학생 정보가 있는 계정으로 OAuth 인증을 시도할 때") {
                    val tokenResponse =
                        GoogleTokenResDto(
                            accessToken = accessToken,
                            expiresIn = 3600,
                            scope = "email profile",
                            tokenType = "Bearer",
                        )

                    val userInfo =
                        GoogleUserInfoResDto(
                            id = "123456",
                            email = email,
                            verifiedEmail = true,
                            name = "Student User",
                            picture = "https://example.com/picture.jpg",
                        )

                    val student =
                        StudentJpaEntity().apply {
                            id = 1L
                            this.email = email
                            role = StudentRole.STUDENT_COUNCIL
                        }

                    val accountWithStudent =
                        AccountJpaEntity.create(email).apply {
                            id = 1L
                            this.student = student
                        }

                    beforeEach {
                        every { mockClientRegistrationRepository.findByRegistrationId("google") } returns
                            clientRegistration
                        every { mockGoogleOAuth2Client.exchangeCodeForToken(any()) } returns tokenResponse
                        every { mockGoogleUserInfoClient.getUserInfo("Bearer $accessToken") } returns userInfo
                        every { mockAccountRepository.findByEmail(email) } returns
                            Optional.of(accountWithStudent)
                        every { mockJwtProvider.generateAccessToken(email, AccountRole.USER) } returns
                            jwtAccessToken
                        every { mockJwtProvider.generateRefreshToken(email) } returns jwtRefreshToken
                        every { mockRefreshTokenRepository.deleteByEmail(email) } returns Unit
                        every { mockRefreshTokenRepository.save(any()) } returns mockk()
                    }

                    it("학생의 역할로 토큰을 발급해야 한다") {
                        val result = authenticateGoogleOAuthService.execute(authCode)

                        result.accessToken shouldBe jwtAccessToken
                        result.refreshToken shouldBe jwtRefreshToken

                        verify(exactly = 1) { mockJwtProvider.generateAccessToken(email, AccountRole.USER) }
                        verify(exactly = 1) { mockRefreshTokenRepository.save(any()) }
                    }
                }

                context("기존 refresh token이 있을 때") {
                    val tokenResponse =
                        GoogleTokenResDto(
                            accessToken = accessToken,
                            expiresIn = 3600,
                            scope = "email profile",
                            tokenType = "Bearer",
                        )

                    val userInfo =
                        GoogleUserInfoResDto(
                            id = "123456",
                            email = email,
                            verifiedEmail = true,
                            name = "Test User",
                            picture = "https://example.com/picture.jpg",
                        )

                    val existingAccount =
                        AccountJpaEntity.create(email).apply {
                            id = 1L
                        }

                    beforeEach {
                        every { mockClientRegistrationRepository.findByRegistrationId("google") } returns
                            clientRegistration
                        every { mockGoogleOAuth2Client.exchangeCodeForToken(any()) } returns tokenResponse
                        every { mockGoogleUserInfoClient.getUserInfo("Bearer $accessToken") } returns userInfo
                        every { mockAccountRepository.findByEmail(email) } returns
                            Optional.of(existingAccount)
                        every { mockJwtProvider.generateAccessToken(email, AccountRole.USER) } returns
                            jwtAccessToken
                        every { mockJwtProvider.generateRefreshToken(email) } returns jwtRefreshToken
                        every { mockRefreshTokenRepository.deleteByEmail(email) } returns Unit
                        every { mockRefreshTokenRepository.save(any()) } returns mockk()
                    }

                    it("기존 refresh token을 삭제하고 새로 발급해야 한다") {
                        authenticateGoogleOAuthService.execute(authCode)

                        verify(exactly = 1) { mockRefreshTokenRepository.deleteByEmail(email) }
                        verify(exactly = 1) { mockRefreshTokenRepository.save(any()) }
                    }
                }
            }
        }
    })
