package team.themoment.datagsm.web.domain.event.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.event.entity.EventJpaEntity
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.web.domain.event.service.impl.QueryEventServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import java.time.LocalDateTime

class QueryEventServiceTest :
    DescribeSpec({

        lateinit var eventJpaRepository: EventJpaRepository
        lateinit var currentUserProvider: CurrentUserProvider
        lateinit var queryEventService: QueryEventService
        lateinit var account: AccountJpaEntity

        beforeEach {
            eventJpaRepository = mockk()
            currentUserProvider = mockk()
            account = mockk()
            every { currentUserProvider.getCurrentAccount() } returns account
            queryEventService = QueryEventServiceImpl(eventJpaRepository, currentUserProvider)
        }

        describe("QueryEventService 클래스의") {
            describe("execute 메서드는") {
                context("등록된 Event가 있을 때") {
                    beforeEach {
                        val event =
                            EventJpaEntity().apply {
                                id = 1L
                                targetUrl = "https://example.com/event"
                                events = mutableSetOf(EventType.CLUB_CHANGED)
                                this.account = account
                                createdAt = LocalDateTime.now()
                            }
                        every { eventJpaRepository.findAllByAccount(account) } returns listOf(event)
                    }

                    it("Event 목록을 반환해야 한다") {
                        val result = queryEventService.execute()

                        result.events.size shouldBe 1
                        result.events[0].id shouldBe 1L
                        result.events[0].targetUrl shouldBe "https://example.com/event"
                        result.events[0].events shouldBe setOf(EventType.CLUB_CHANGED)
                    }
                }

                context("등록된 Event가 없을 때") {
                    beforeEach {
                        every { eventJpaRepository.findAllByAccount(account) } returns emptyList()
                    }

                    it("빈 목록을 반환해야 한다") {
                        val result = queryEventService.execute()

                        result.events.size shouldBe 0
                    }
                }
            }
        }
    })
