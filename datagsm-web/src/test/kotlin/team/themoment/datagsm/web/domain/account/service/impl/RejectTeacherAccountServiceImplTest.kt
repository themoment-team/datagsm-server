package team.themoment.datagsm.web.domain.account.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.teacher.repository.TeacherJpaRepository
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class RejectTeacherAccountServiceImplTest :
    DescribeSpec({

        val accountJpaRepository = mockk<AccountJpaRepository>()
        val teacherJpaRepository = mockk<TeacherJpaRepository>()
        val service = RejectTeacherAccountServiceImpl(accountJpaRepository, teacherJpaRepository)

        afterEach {
            clearAllMocks()
        }

        describe("RejectTeacherAccountService 클래스의") {
            describe("execute 메서드는") {

                context("승인 대기 중인 선생님 계정을 거절할 때") {
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
                        every { teacherJpaRepository.deleteById(10L) } just runs
                        every { accountJpaRepository.delete(account) } just runs
                    }

                    it("선생님 정보와 계정이 삭제되어야 한다") {
                        service.execute(1L)

                        verify(exactly = 1) { teacherJpaRepository.deleteById(10L) }
                        verify(exactly = 1) { accountJpaRepository.delete(account) }
                    }
                }

                context("존재하지 않는 계정을 거절할 때") {
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

                context("선생님이 아닌 계정을 거절할 때") {
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

                    it("BAD_REQUEST ExpectedException이 발생하고 아무것도 삭제되지 않아야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(2L)
                            }
                        exception.message shouldBe "선생님 계정이 아닙니다."
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                        verify(exactly = 0) { teacherJpaRepository.deleteById(any()) }
                        verify(exactly = 0) { accountJpaRepository.delete(any()) }
                    }
                }

                context("이미 승인된 선생님 계정을 거절할 때") {
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

                    it("CONFLICT ExpectedException이 발생하고 아무것도 삭제되지 않아야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(3L)
                            }
                        exception.message shouldBe "이미 승인된 계정입니다."
                        exception.statusCode shouldBe HttpStatus.CONFLICT
                        verify(exactly = 0) { teacherJpaRepository.deleteById(any()) }
                        verify(exactly = 0) { accountJpaRepository.delete(any()) }
                    }
                }
            }
        }
    })
