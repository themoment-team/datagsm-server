package team.themoment.datagsm.web.domain.client.validator

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.entity.OAuthScopeJpaEntity
import team.themoment.datagsm.common.domain.application.repository.OAuthScopeJpaRepository
import team.themoment.sdk.exception.ExpectedException

class OauthScopeValidatorTest :
    DescribeSpec({

        lateinit var mockOAuthScopeJpaRepository: OAuthScopeJpaRepository
        lateinit var oauthScopeValidator: OauthScopeValidator

        fun buildScope(
            applicationId: String,
            name: String,
        ): OAuthScopeJpaEntity =
            OAuthScopeJpaEntity().apply {
                scopeName = name
                description = ""
                application = ApplicationJpaEntity().apply { id = applicationId }
            }

        beforeEach {
            mockOAuthScopeJpaRepository = mockk<OAuthScopeJpaRepository>()
            oauthScopeValidator = OauthScopeValidator(mockOAuthScopeJpaRepository)
        }

        describe("OauthScopeValidator 클래스의") {
            describe("validate 메서드는") {

                context("요청한 scope가 모두 존재할 때") {
                    beforeEach {
                        every { mockOAuthScopeJpaRepository.findAllByApplicationIdIn(setOf("app1", "app2")) } returns
                            listOf(buildScope("app1", "read"), buildScope("app1", "write"), buildScope("app2", "read"))
                    }

                    it("예외 없이 통과하고 요청한 애플리케이션의 scope만 조회해야 한다") {
                        shouldNotThrowAny {
                            oauthScopeValidator.validate(setOf("app1:read", "app2:read"))
                        }

                        verify(exactly = 1) { mockOAuthScopeJpaRepository.findAllByApplicationIdIn(setOf("app1", "app2")) }
                    }
                }

                context("존재하지 않는 scope가 포함되어 있을 때") {
                    beforeEach {
                        every { mockOAuthScopeJpaRepository.findAllByApplicationIdIn(setOf("app1")) } returns
                            listOf(buildScope("app1", "read"))
                    }

                    it("400 ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                oauthScopeValidator.validate(setOf("app1:read", "app1:unknown"))
                            }

                        exception.message shouldBe "허용되지 않는 OAuth 권한이 포함되어 있습니다."
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("형식이 잘못된 scope가 포함되어 있을 때") {
                    beforeEach {
                        every { mockOAuthScopeJpaRepository.findAllByApplicationIdIn(setOf("app1", "invalid")) } returns
                            listOf(buildScope("app1", "read"))
                    }

                    it("400 ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                oauthScopeValidator.validate(setOf("app1:read", "invalid"))
                            }

                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("빈 scope 목록일 때") {
                    it("DB를 조회하지 않고 통과해야 한다") {
                        shouldNotThrowAny {
                            oauthScopeValidator.validate(emptySet())
                        }

                        verify(exactly = 0) { mockOAuthScopeJpaRepository.findAllByApplicationIdIn(any()) }
                    }
                }
            }
        }
    })
