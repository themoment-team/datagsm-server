package team.themoment.datagsm.web.domain.account.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class ApproveTeacherAccountServiceImplTest :
    DescribeSpec({

        val accountJpaRepository = mockk<AccountJpaRepository>()
        val service = ApproveTeacherAccountServiceImpl(accountJpaRepository)

        afterEach {
            clearAllMocks()
        }

        describe("ApproveTeacherAccountService 클래스의") {
            describe("execute 메서드는") {

                context("승인 대기 중인 선생님 계정을 승인할 때") {
                    val account =
                        AccountJpaEntity().apply {
                            id = 1L
                            email = "teacher@gsm.hs.kr"
                            password = "encoded"
                            objectId = 10L
                            objectType = AccountObjectType.TEACHER
                            status = AccountStatus.PENDING
                        }

                    beforeEach {
                        every { accountJpaRepository.findById(1L) } returns Optional.of(account)
                    }

                    it("계정 상태가 ACTIVE로 변경되어야 한다") {
                        service.execute(1L)

                        account.status shouldBe AccountStatus.ACTIVE
                    }
                }

                context("존재하지 않는 계정을 승인할 때") {
                    beforeEach {
                        every { accountJpaRepository.findById(999L) } returns Optional.empty()
                    }

                    it("NOT_FOUND ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(999L)
                            }
                        exception.message shouldBe "계정을 찾을 수 없습니다."
                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                    }
                }

                context("선생님이 아닌 계정을 승인할 때") {
                    val account =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = "student@gsm.hs.kr"
                            password = "encoded"
                            objectId = 20L
                            objectType = AccountObjectType.STUDENT
                            status = AccountStatus.ACTIVE
                        }

                    beforeEach {
                        every { accountJpaRepository.findById(2L) } returns Optional.of(account)
                    }

                    it("BAD_REQUEST ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(2L)
                            }
                        exception.message shouldBe "선생님 계정이 아닙니다."
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("이미 승인된 선생님 계정을 승인할 때") {
                    val account =
                        AccountJpaEntity().apply {
                            id = 3L
                            email = "active@gsm.hs.kr"
                            password = "encoded"
                            objectId = 30L
                            objectType = AccountObjectType.TEACHER
                            status = AccountStatus.ACTIVE
                        }

                    beforeEach {
                        every { accountJpaRepository.findById(3L) } returns Optional.of(account)
                    }

                    it("CONFLICT ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(3L)
                            }
                        exception.message shouldBe "이미 승인된 계정입니다."
                        exception.statusCode shouldBe HttpStatus.CONFLICT
                    }
                }
            }
        }
    })
