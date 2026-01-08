package team.themoment.datagsm.web.domain.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.AccountRole
import team.themoment.datagsm.common.domain.account.RefreshTokenRedisEntity
import team.themoment.datagsm.common.domain.student.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.StudentRole
import team.themoment.datagsm.web.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.web.domain.auth.repository.RefreshTokenRedisRepository
import team.themoment.datagsm.web.domain.auth.service.impl.ReissueTokenServiceImpl
import team.themoment.datagsm.web.global.security.jwt.JwtProperties
import team.themoment.datagsm.web.global.security.jwt.JwtProvider
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class ReissueTokenServiceTest :
    DescribeSpec({

        val mockJwtProvider = mockk<JwtProvider>()
        val mockJwtProperties = mockk<JwtProperties>()
        val mockRefreshTokenRepository = mockk<RefreshTokenRedisRepository>()
        val mockAccountRepository = mockk<AccountJpaRepository>()

        val reissueTokenService =
            ReissueTokenServiceImpl(
                mockJwtProvider,
                mockJwtProperties,
                mockRefreshTokenRepository,
                mockAccountRepository,
            )

        afterEach {
            clearAllMocks()
        }

        describe("ReissueTokenService 클래스의") {
            describe("execute 메서드는") {

                val email = "test@gsm.hs.kr"
                val refreshToken = "valid.refresh.token"
                val newAccessToken = "new.access.token"
                val newRefreshToken = "new.refresh.token"

                beforeEach {
                    every { mockJwtProperties.refreshTokenExpiration } returns 604800000L
                }

                context("유효하지 않은 refresh token으로 요청할 때") {
                    beforeEach {
                        every { mockJwtProvider.validateToken(refreshToken) } returns false
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                reissueTokenService.execute(refreshToken)
                            }

                        exception.message shouldBe "유효하지 않은 refresh token입니다."
                        exception.statusCode.value() shouldBe 401

                        verify(exactly = 1) { mockJwtProvider.validateToken(refreshToken) }
                        verify(exactly = 0) { mockRefreshTokenRepository.findByEmail(any()) }
                    }
                }

                context("저장된 refresh token을 찾을 수 없을 때") {
                    beforeEach {
                        every { mockJwtProvider.validateToken(refreshToken) } returns true
                        every { mockJwtProvider.getEmailFromToken(refreshToken) } returns email
                        every { mockRefreshTokenRepository.findByEmail(email) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                reissueTokenService.execute(refreshToken)
                            }

                        exception.message shouldBe "저장된 refresh token을 찾을 수 없습니다."
                        exception.statusCode.value() shouldBe 401

                        verify(exactly = 1) { mockJwtProvider.validateToken(refreshToken) }
                        verify(exactly = 1) { mockJwtProvider.getEmailFromToken(refreshToken) }
                        verify(exactly = 1) { mockRefreshTokenRepository.findByEmail(email) }
                    }
                }

                context("저장된 토큰과 요청 토큰이 일치하지 않을 때") {
                    val storedToken =
                        RefreshTokenRedisEntity.of(
                            email = email,
                            token = "different.token",
                            ttl = 604800L,
                        )

                    beforeEach {
                        every { mockJwtProvider.validateToken(refreshToken) } returns true
                        every { mockJwtProvider.getEmailFromToken(refreshToken) } returns email
                        every { mockRefreshTokenRepository.findByEmail(email) } returns Optional.of(storedToken)
                        every { mockRefreshTokenRepository.deleteByEmail(email) } returns Unit
                    }

                    it("토큰을 삭제하고 ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                reissueTokenService.execute(refreshToken)
                            }

                        exception.message shouldBe "Refresh token이 일치하지 않습니다. 재로그인이 필요합니다."
                        exception.statusCode.value() shouldBe 401

                        verify(exactly = 1) { mockRefreshTokenRepository.deleteByEmail(email) }
                        verify(exactly = 0) { mockAccountRepository.findByEmail(any()) }
                    }
                }

                context("계정을 찾을 수 없을 때") {
                    val storedToken =
                        RefreshTokenRedisEntity.of(
                            email = email,
                            token = refreshToken,
                            ttl = 604800L,
                        )

                    beforeEach {
                        every { mockJwtProvider.validateToken(refreshToken) } returns true
                        every { mockJwtProvider.getEmailFromToken(refreshToken) } returns email
                        every { mockRefreshTokenRepository.findByEmail(email) } returns Optional.of(storedToken)
                        every { mockAccountRepository.findByEmail(email) } returns Optional.empty()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                reissueTokenService.execute(refreshToken)
                            }

                        exception.message shouldBe "계정을 찾을 수 없습니다."
                        exception.statusCode.value() shouldBe 404

                        verify(exactly = 1) { mockAccountRepository.findByEmail(email) }
                        verify(exactly = 0) { mockJwtProvider.generateAccessToken(any(), any()) }
                    }
                }

                context("정상적으로 토큰을 재발급할 때") {
                    val storedToken =
                        RefreshTokenRedisEntity.of(
                            email = email,
                            token = refreshToken,
                            ttl = 604800L,
                        )

                    val account =
                        AccountJpaEntity.create(email).apply {
                            id = 1L
                        }

                    beforeEach {
                        every { mockJwtProvider.validateToken(refreshToken) } returns true
                        every { mockJwtProvider.getEmailFromToken(refreshToken) } returns email
                        every { mockRefreshTokenRepository.findByEmail(email) } returns Optional.of(storedToken)
                        every { mockAccountRepository.findByEmail(email) } returns Optional.of(account)
                        every { mockJwtProvider.generateAccessToken(email, AccountRole.USER) } returns
                            newAccessToken
                        every { mockJwtProvider.generateRefreshToken(email) } returns newRefreshToken
                        every { mockRefreshTokenRepository.deleteByEmail(email) } returns Unit
                        every { mockRefreshTokenRepository.save(any()) } returns mockk()
                    }

                    it("새로운 토큰을 발급하고 반환해야 한다") {
                        val result = reissueTokenService.execute(refreshToken)

                        result.accessToken shouldBe newAccessToken
                        result.refreshToken shouldBe newRefreshToken

                        verify(exactly = 1) { mockJwtProvider.validateToken(refreshToken) }
                        verify(exactly = 1) { mockJwtProvider.getEmailFromToken(refreshToken) }
                        verify(exactly = 1) { mockRefreshTokenRepository.findByEmail(email) }
                        verify(exactly = 1) { mockAccountRepository.findByEmail(email) }
                        verify(exactly = 1) { mockJwtProvider.generateAccessToken(email, AccountRole.USER) }
                        verify(exactly = 1) { mockJwtProvider.generateRefreshToken(email) }
                        verify(exactly = 1) { mockRefreshTokenRepository.deleteByEmail(email) }
                        verify(exactly = 1) { mockRefreshTokenRepository.save(any()) }
                    }
                }

                context("학생 정보가 있는 계정으로 토큰 재발급할 때") {
                    val storedToken =
                        RefreshTokenRedisEntity.of(
                            email = email,
                            token = refreshToken,
                            ttl = 604800L,
                        )

                    val student =
                        StudentJpaEntity().apply {
                            id = 1L
                            this.email = email
                            role = StudentRole.STUDENT_COUNCIL
                        }

                    val account =
                        AccountJpaEntity.create(email).apply {
                            id = 1L
                            this.student = student
                        }

                    beforeEach {
                        every { mockJwtProvider.validateToken(refreshToken) } returns true
                        every { mockJwtProvider.getEmailFromToken(refreshToken) } returns email
                        every { mockRefreshTokenRepository.findByEmail(email) } returns Optional.of(storedToken)
                        every { mockAccountRepository.findByEmail(email) } returns Optional.of(account)
                        every { mockJwtProvider.generateAccessToken(email, AccountRole.USER) } returns
                            newAccessToken
                        every { mockJwtProvider.generateRefreshToken(email) } returns newRefreshToken
                        every { mockRefreshTokenRepository.deleteByEmail(email) } returns Unit
                        every { mockRefreshTokenRepository.save(any()) } returns mockk()
                    }

                    it("학생의 역할로 새로운 토큰을 발급해야 한다") {
                        val result = reissueTokenService.execute(refreshToken)

                        result.accessToken shouldBe newAccessToken
                        result.refreshToken shouldBe newRefreshToken

                        verify(exactly = 1) { mockJwtProvider.generateAccessToken(email, AccountRole.USER) }
                        verify(exactly = 1) { mockRefreshTokenRepository.save(any()) }
                    }
                }
            }
        }
    })
