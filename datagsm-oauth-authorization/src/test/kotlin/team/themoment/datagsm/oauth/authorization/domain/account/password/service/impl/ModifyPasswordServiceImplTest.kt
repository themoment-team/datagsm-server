package team.themoment.datagsm.oauth.authorization.domain.account.password.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder
import team.themoment.datagsm.common.domain.account.dto.request.ChangePasswordReqDto
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.PasswordResetCodeRedisEntity
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.repository.PasswordResetCodeRedisRepository
import team.themoment.datagsm.common.domain.oauth.entity.OauthRefreshTokenRedisEntity
import team.themoment.datagsm.common.domain.oauth.repository.OauthRefreshTokenRedisRepository
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class ModifyPasswordServiceImplTest :
    BehaviorSpec({
        val passwordResetCodeRedisRepository = mockk<PasswordResetCodeRedisRepository>(relaxed = true)
        val accountJpaRepository = mockk<AccountJpaRepository>()
        val passwordEncoder = mockk<PasswordEncoder>()
        val oauthRefreshTokenRedisRepository = mockk<OauthRefreshTokenRedisRepository>(relaxed = true)

        val service =
            ModifyPasswordServiceImpl(
                passwordResetCodeRedisRepository,
                accountJpaRepository,
                passwordEncoder,
                oauthRefreshTokenRedisRepository,
            )

        Given("verified가 false일 때") {
            val email = "user@gsm.hs.kr"
            val code = "12345678"
            val newPassword = "newPassword123!"
            val reqDto = ChangePasswordReqDto(email = email, code = code, newPassword = newPassword)
            val redisEntity =
                PasswordResetCodeRedisEntity(
                    email = email,
                    code = code,
                    verified = false,
                    ttl = 300,
                )

            every { passwordResetCodeRedisRepository.findById(email) } returns Optional.of(redisEntity)

            When("비밀번호 변경을 요청하면") {
                Then("400 Bad Request 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(reqDto)
                        }

                    exception.message shouldBe "인증 코드 검증이 필요합니다."
                }
            }
        }

        Given("코드가 일치하지 않을 때") {
            val email = "user@gsm.hs.kr"
            val correctCode = "12345678"
            val wrongCode = "87654321"
            val newPassword = "newPassword123!"
            val reqDto = ChangePasswordReqDto(email = email, code = wrongCode, newPassword = newPassword)
            val redisEntity =
                PasswordResetCodeRedisEntity(
                    email = email,
                    code = correctCode,
                    verified = true,
                    ttl = 300,
                )

            every { passwordResetCodeRedisRepository.findById(email) } returns Optional.of(redisEntity)

            When("비밀번호 변경을 요청하면") {
                Then("400 Bad Request 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(reqDto)
                        }

                    exception.message shouldBe "인증 코드가 일치하지 않습니다."
                }
            }
        }

        Given("존재하지 않는 계정으로") {
            val email = "nonexistent@gsm.hs.kr"
            val code = "12345678"
            val newPassword = "newPassword123!"
            val reqDto = ChangePasswordReqDto(email = email, code = code, newPassword = newPassword)
            val redisEntity =
                PasswordResetCodeRedisEntity(
                    email = email,
                    code = code,
                    verified = true,
                    ttl = 300,
                )

            every { passwordResetCodeRedisRepository.findById(email) } returns Optional.of(redisEntity)
            every { accountJpaRepository.findByEmail(email) } returns Optional.empty()

            When("비밀번호 변경을 요청하면") {
                Then("404 Not Found 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(reqDto)
                        }

                    exception.message shouldBe "존재하지 않는 이메일입니다."
                }
            }
        }

        Given("이전 비밀번호와 동일한 비밀번호로") {
            val email = "user@gsm.hs.kr"
            val code = "12345678"
            val oldPassword = "oldPassword123!"
            val newPassword = "oldPassword123!"
            val reqDto = ChangePasswordReqDto(email = email, code = code, newPassword = newPassword)
            val redisEntity =
                PasswordResetCodeRedisEntity(
                    email = email,
                    code = code,
                    verified = true,
                    ttl = 300,
                )
            val account =
                AccountJpaEntity().apply {
                    this.email = email
                    this.password = "hashedOldPassword"
                }

            every { passwordResetCodeRedisRepository.findById(email) } returns Optional.of(redisEntity)
            every { accountJpaRepository.findByEmail(email) } returns Optional.of(account)
            every { passwordEncoder.matches(newPassword, account.password) } returns true

            When("비밀번호 변경을 요청하면") {
                Then("400 Bad Request 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(reqDto)
                        }

                    exception.message shouldBe "이전 비밀번호와 동일한 비밀번호는 사용할 수 없습니다."
                }
            }
        }

        Given("검증된 인증 코드로") {
            val email = "user@gsm.hs.kr"
            val code = "12345678"
            val oldPassword = "oldPassword123!"
            val newPassword = "newPassword123!"
            val reqDto = ChangePasswordReqDto(email = email, code = code, newPassword = newPassword)
            val redisEntity =
                PasswordResetCodeRedisEntity(
                    email = email,
                    code = code,
                    verified = true,
                    ttl = 300,
                )
            val account =
                AccountJpaEntity().apply {
                    this.email = email
                    this.password = "hashedOldPassword"
                }
            val token1 =
                OauthRefreshTokenRedisEntity.of(
                    email = email,
                    clientId = "client1",
                    token = "token1",
                    ttl = 3600,
                )
            val token2 =
                OauthRefreshTokenRedisEntity.of(
                    email = email,
                    clientId = "client2",
                    token = "token2",
                    ttl = 3600,
                )

            every { passwordResetCodeRedisRepository.findById(email) } returns Optional.of(redisEntity)
            every { accountJpaRepository.findByEmail(email) } returns Optional.of(account)
            every { passwordEncoder.matches(newPassword, account.password) } returns false
            every { passwordEncoder.encode(newPassword) } returns "hashedNewPassword"
            every { accountJpaRepository.save(any()) } returns account
            every { oauthRefreshTokenRedisRepository.findAllByEmail(email) } returns listOf(token1, token2)
            every { oauthRefreshTokenRedisRepository.deleteAll(any<Iterable<OauthRefreshTokenRedisEntity>>()) } returns Unit

            When("새 비밀번호로 변경하면") {
                service.execute(reqDto)

                Then("비밀번호가 변경되고 모든 작업이 수행된다") {
                    account.password shouldBe "hashedNewPassword"
                    verify(exactly = 1) { accountJpaRepository.save(account) }
                    verify(exactly = 1) { passwordResetCodeRedisRepository.deleteById(email) }
                    verify(exactly = 1) { oauthRefreshTokenRedisRepository.deleteAll(any<Iterable<OauthRefreshTokenRedisEntity>>()) }
                    verify(exactly = 1) { passwordEncoder.encode(newPassword) }
                    verify(exactly = 1) { passwordEncoder.matches(newPassword, "hashedOldPassword") }
                }
            }
        }
    })
