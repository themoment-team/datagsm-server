package team.themoment.datagsm.web.domain.event.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.event.entity.EventJpaEntity
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.web.domain.event.service.impl.DeleteEventServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime

class DeleteEventServiceTest :
    DescribeSpec({

        lateinit var eventJpaRepository: EventJpaRepository
        lateinit var currentUserProvider: CurrentUserProvider
        lateinit var deleteEventService: DeleteEventService
        lateinit var account: AccountJpaEntity

        beforeEach {
            eventJpaRepository = mockk()
            currentUserProvider = mockk()
            account = mockk()
            every { currentUserProvider.getCurrentAccount() } returns account
            deleteEventService = DeleteEventServiceImpl(eventJpaRepository, currentUserProvider)
        }

        describe("DeleteEventService 클래스의") {
            describe("execute 메서드는") {
                context("Event가 존재하지 않을 때") {
                    beforeEach {
                        every { eventJpaRepository.findByIdAndAccount(1L, account) } returns null
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                deleteEventService.execute(1L)
                            }
                        ex.message shouldBe "Event를 찾을 수 없습니다."
                    }
                }

                context("Event가 존재할 때") {
                    val event =
                        EventJpaEntity().apply {
                            id = 1L
                            targetUrl = "https://example.com/event"
                            events = mutableSetOf(EventType.CLUB_UPDATED)
                            createdAt = LocalDateTime.now()
                        }

                    beforeEach {
                        every { eventJpaRepository.findByIdAndAccount(1L, account) } returns event
                        justRun { eventJpaRepository.delete(event) }
                    }

                    it("Event를 삭제해야 한다") {
                        deleteEventService.execute(1L)

                        verify(exactly = 1) { eventJpaRepository.delete(event) }
                    }
                }
            }
        }
    })
