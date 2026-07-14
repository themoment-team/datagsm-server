package team.themoment.datagsm.web.domain.account.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
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
    BehaviorSpec({
        val accountJpaRepository = mockk<AccountJpaRepository>()
        val service = ApproveTeacherAccountServiceImpl(accountJpaRepository)

        Given("승인 대기 중인 선생님 계정을") {
            val account =
                AccountJpaEntity().apply {
                    id = 1L
                    email = "teacher@gsm.hs.kr"
                    password = "encoded"
                    objectId = 10L
                    objectType = AccountObjectType.TEACHER
                    status = AccountStatus.PENDING
                }

            every { accountJpaRepository.findById(1L) } returns Optional.of(account)

            When("승인하면") {
                service.execute(1L)

                Then("계정 상태가 ACTIVE로 변경된다") {
                    account.status shouldBe AccountStatus.ACTIVE
                }
            }
        }

        Given("존재하지 않는 계정을") {
            every { accountJpaRepository.findById(999L) } returns Optional.empty()

            When("승인하면") {
                Then("NOT_FOUND 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(999L)
                        }
                    exception.message shouldBe "계정을 찾을 수 없습니다."
                    exception.statusCode shouldBe HttpStatus.NOT_FOUND
                }
            }
        }

        Given("선생님이 아닌 계정을") {
            val account =
                AccountJpaEntity().apply {
                    id = 2L
                    email = "student@gsm.hs.kr"
                    password = "encoded"
                    objectId = 20L
                    objectType = AccountObjectType.STUDENT
                    status = AccountStatus.ACTIVE
                }

            every { accountJpaRepository.findById(2L) } returns Optional.of(account)

            When("승인하면") {
                Then("BAD_REQUEST 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(2L)
                        }
                    exception.message shouldBe "선생님 계정이 아닙니다."
                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                }
            }
        }

        Given("이미 승인된 선생님 계정을") {
            val account =
                AccountJpaEntity().apply {
                    id = 3L
                    email = "active@gsm.hs.kr"
                    password = "encoded"
                    objectId = 30L
                    objectType = AccountObjectType.TEACHER
                    status = AccountStatus.ACTIVE
                }

            every { accountJpaRepository.findById(3L) } returns Optional.of(account)

            When("승인하면") {
                Then("CONFLICT 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(3L)
                        }
                    exception.message shouldBe "이미 승인된 계정입니다."
                    exception.statusCode shouldBe HttpStatus.CONFLICT
                }
            }
        }
    })
