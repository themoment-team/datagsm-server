package team.themoment.datagsm.web.domain.event.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.event.dto.request.ModifyEventReqDto
import team.themoment.datagsm.common.domain.event.entity.EventJpaEntity
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.common.domain.event.validator.EventUrlValidator
import team.themoment.datagsm.web.domain.event.service.impl.ModifyEventServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime

class ModifyEventServiceTest :
    DescribeSpec({

        lateinit var eventJpaRepository: EventJpaRepository
        lateinit var currentUserProvider: CurrentUserProvider
        lateinit var modifyEventService: ModifyEventService
        lateinit var account: AccountJpaEntity

        fun existingEvent() =
            EventJpaEntity().apply {
                id = 1L
                targetUrl = "https://old.example.com/event"
                events = mutableSetOf(EventType.CLUB_UPDATED)
                this.account = account
                createdAt = LocalDateTime.now()
            }

        beforeEach {
            eventJpaRepository = mockk()
            currentUserProvider = mockk()
            account = mockk()
            every { currentUserProvider.getCurrentAccount() } returns account
            modifyEventService = ModifyEventServiceImpl(eventJpaRepository, currentUserProvider)
            mockkObject(EventUrlValidator)
            every { EventUrlValidator.isPrivateOrLocalUrl(any()) } returns false
        }

        afterEach {
            unmockkObject(EventUrlValidator)
        }

        describe("ModifyEventService 클래스의") {
            describe("execute 메서드는") {
                context("Event가 존재하지 않을 때") {
                    val reqDto = ModifyEventReqDto(targetUrl = "https://new.example.com", events = null)

                    beforeEach {
                        every { eventJpaRepository.findByIdAndAccount(1L, account) } returns null
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyEventService.execute(1L, reqDto)
                            }
                        ex.message shouldBe "Event를 찾을 수 없습니다."
                    }
                }

                context("내부 네트워크 URL로 수정하려 할 때") {
                    val reqDto = ModifyEventReqDto(targetUrl = "http://localhost", events = null)

                    beforeEach {
                        every { eventJpaRepository.findByIdAndAccount(1L, account) } returns existingEvent()
                        every { EventUrlValidator.isPrivateOrLocalUrl(any()) } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyEventService.execute(1L, reqDto)
                            }
                        ex.message shouldBe "내부 네트워크 URL은 Event 수신 URL로 등록할 수 없습니다."
                    }
                }

                context("targetUrl만 수정할 때") {
                    val reqDto = ModifyEventReqDto(targetUrl = "https://new.example.com", events = null)

                    beforeEach {
                        every { eventJpaRepository.findByIdAndAccount(1L, account) } returns existingEvent()
                    }

                    it("targetUrl이 변경되고 events는 유지되어야 한다") {
                        val result = modifyEventService.execute(1L, reqDto)

                        result.targetUrl shouldBe "https://new.example.com"
                        result.events shouldBe setOf(EventType.CLUB_UPDATED)
                    }
                }

                context("events만 수정할 때") {
                    val reqDto =
                        ModifyEventReqDto(targetUrl = null, events = setOf(EventType.PROJECT_UPDATED))

                    beforeEach {
                        every { eventJpaRepository.findByIdAndAccount(1L, account) } returns existingEvent()
                    }

                    it("events가 교체되고 targetUrl은 유지되어야 한다") {
                        val result = modifyEventService.execute(1L, reqDto)

                        result.targetUrl shouldBe "https://old.example.com/event"
                        result.events shouldBe setOf(EventType.PROJECT_UPDATED)
                    }
                }
            }
        }
    })
