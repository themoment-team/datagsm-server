package team.themoment.datagsm.web.domain.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.entity.ThirdPartyScopeJpaEntity
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.web.domain.application.service.impl.QueryApplicationServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class QueryApplicationServiceTest :
    DescribeSpec({

        val mockApplicationJpaRepository = mockk<ApplicationJpaRepository>()

        val service = QueryApplicationServiceImpl(mockApplicationJpaRepository)

        afterEach {
            clearAllMocks()
        }

        describe("QueryApplicationService 클래스의") {
            describe("execute 메서드는") {

                val ownerAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = "owner@gsm.hs.kr"
                        role = AccountRole.USER
                    }

                context("존재하는 Application ID로 조회할 때") {
                    val applicationId = "app-uuid-1234"

                    val scope1 =
                        ThirdPartyScopeJpaEntity().apply {
                            id = 1L
                            scopeName = "profile"
                            description = "사용자 프로필 정보 조회"
                        }

                    val application =
                        ApplicationJpaEntity().apply {
                            id = applicationId
                            name = "My Application"
                            account = ownerAccount
                            thirdPartyScopes = mutableListOf(scope1)
                        }
                    scope1.application = application

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(application)
                    }

                    it("Application 정보가 반환되어야 한다") {
                        val result = service.execute(applicationId)

                        result.id shouldBe applicationId
                        result.name shouldBe "My Application"
                        result.accountId shouldBe 1L
                        result.scopes.size shouldBe 1
                        result.scopes[0].scopeName shouldBe "profile"
                        result.scopes[0].description shouldBe "사용자 프로필 정보 조회"

                        verify(exactly = 1) { mockApplicationJpaRepository.findById(applicationId) }
                    }
                }

                context("스코프가 없는 Application을 조회할 때") {
                    val applicationId = "app-no-scopes"

                    val application =
                        ApplicationJpaEntity().apply {
                            id = applicationId
                            name = "Empty Application"
                            account = ownerAccount
                            thirdPartyScopes = mutableListOf()
                        }

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(applicationId) } returns Optional.of(application)
                    }

                    it("빈 스코프 목록이 반환되어야 한다") {
                        val result = service.execute(applicationId)

                        result.scopes shouldBe emptyList()
                    }
                }

                context("존재하지 않는 Application ID로 조회할 때") {
                    val nonExistingId = "non-existing-id"

                    beforeEach {
                        every { mockApplicationJpaRepository.findById(nonExistingId) } returns Optional.empty()
                    }

                    it("404 NOT_FOUND 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(nonExistingId)
                            }

                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                        exception.message shouldBe "Application을 찾을 수 없습니다."

                        verify(exactly = 1) { mockApplicationJpaRepository.findById(nonExistingId) }
                    }
                }
            }
        }
    })
