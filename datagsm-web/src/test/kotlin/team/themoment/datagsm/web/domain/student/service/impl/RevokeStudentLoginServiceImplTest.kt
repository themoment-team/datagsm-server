package team.themoment.datagsm.web.domain.student.service.impl

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.oauth.entity.IdpSessionRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.OauthRefreshTokenRedisEntity
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthRefreshTokenRedisRepository

class RevokeStudentLoginServiceImplTest :
    DescribeSpec({

        lateinit var mockAccountJpaRepository: AccountJpaRepository
        lateinit var mockIdpSessionRedisRepository: IdpSessionRedisRepository
        lateinit var mockOauthRefreshTokenRedisRepository: OauthRefreshTokenRedisRepository
        lateinit var service: RevokeStudentLoginServiceImpl

        val studentId = 210L
        val email = "s25012@gsm.hs.kr"

        val account =
            AccountJpaEntity().apply {
                id = 13L
                this.email = email
                objectType = AccountObjectType.STUDENT
                objectId = studentId
            }
        val idpSession = IdpSessionRedisEntity(sessionId = "session-1", email = email, ttl = 28800L)
        val refreshToken =
            OauthRefreshTokenRedisEntity.of(
                email = email,
                clientId = "datagsm-web",
                token = "refresh-token",
                scopes = setOf("self:read"),
                ttl = 2592000L,
            )

        beforeEach {
            mockAccountJpaRepository = mockk()
            mockIdpSessionRedisRepository = mockk()
            mockOauthRefreshTokenRedisRepository = mockk()
            service =
                RevokeStudentLoginServiceImpl(
                    mockAccountJpaRepository,
                    mockIdpSessionRedisRepository,
                    mockOauthRefreshTokenRedisRepository,
                )

            justRun { mockIdpSessionRedisRepository.deleteAll(any<Iterable<IdpSessionRedisEntity>>()) }
            justRun { mockOauthRefreshTokenRedisRepository.deleteAll(any<Iterable<OauthRefreshTokenRedisEntity>>()) }
        }

        describe("RevokeStudentLoginServiceImpl 클래스의") {
            describe("revokeAsync 메서드는") {
                context("대상 학생에게 연결된 계정이 로그인 중일 때") {
                    beforeEach {
                        every {
                            mockAccountJpaRepository.findAllByObjectTypeAndObjectIdIn(AccountObjectType.STUDENT, listOf(studentId))
                        } returns listOf(account)
                        every { mockIdpSessionRedisRepository.findAllByEmail(email) } returns listOf(idpSession)
                        every { mockOauthRefreshTokenRedisRepository.findAllByEmail(email) } returns listOf(refreshToken)
                    }

                    it("해당 계정의 IdP 세션과 refresh token을 모두 폐기한다") {
                        service.revokeAsync(listOf(studentId))

                        verify(exactly = 1) { mockIdpSessionRedisRepository.deleteAll(listOf(idpSession)) }
                        verify(exactly = 1) { mockOauthRefreshTokenRedisRepository.deleteAll(listOf(refreshToken)) }
                    }
                }

                context("대상 학생에게 연결된 계정이 없을 때") {
                    beforeEach {
                        every {
                            mockAccountJpaRepository.findAllByObjectTypeAndObjectIdIn(AccountObjectType.STUDENT, listOf(studentId))
                        } returns emptyList()
                    }

                    it("폐기할 세션이 없으므로 Redis를 조회하지 않는다") {
                        service.revokeAsync(listOf(studentId))

                        verify(exactly = 0) { mockIdpSessionRedisRepository.findAllByEmail(any()) }
                        verify(exactly = 0) { mockOauthRefreshTokenRedisRepository.findAllByEmail(any()) }
                    }
                }

                context("대상 학생 목록이 비어 있을 때") {
                    it("계정을 조회하지 않는다") {
                        service.revokeAsync(emptyList())

                        verify(exactly = 0) { mockAccountJpaRepository.findAllByObjectTypeAndObjectIdIn(any(), any()) }
                    }
                }
            }
        }
    })
