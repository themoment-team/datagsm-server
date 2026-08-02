package team.themoment.datagsm.web.domain.event.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.event.dto.request.ModifyEventReqDto
import team.themoment.datagsm.common.domain.event.entity.EventJpaEntity
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.entity.constant.EventVerificationStatus
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.web.domain.event.dto.internal.EventVerificationRequested
import team.themoment.datagsm.web.domain.event.service.impl.ModifyEventServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime

class ModifyEventServiceTest :
    DescribeSpec({

        lateinit var eventJpaRepository: EventJpaRepository
        lateinit var currentUserProvider: CurrentUserProvider
        lateinit var applicationEventPublisher: ApplicationEventPublisher
        lateinit var modifyEventService: ModifyEventService
        lateinit var account: AccountJpaEntity

        fun existingEvent(status: EventVerificationStatus = EventVerificationStatus.VERIFIED) =
            EventJpaEntity().apply {
                id = 1L
                targetUrl = "https://old.example.com/event"
                events = mutableSetOf(EventType.CLUB_UPDATED)
                this.account = account
                verificationStatus = status
                createdAt = LocalDateTime.now()
            }

        beforeEach {
            eventJpaRepository = mockk()
            currentUserProvider = mockk()
            applicationEventPublisher = mockk(relaxed = true)
            account = mockk()
            every { currentUserProvider.getCurrentAccount() } returns account
            modifyEventService =
                ModifyEventServiceImpl(eventJpaRepository, currentUserProvider, applicationEventPublisher)
        }

        describe("ModifyEventService 클래스의") {
            describe("execute 메서드는") {
                context("Event가 존재하지 않을 때") {
                    val reqDto = ModifyEventReqDto(targetUrl = "https://new.example.com", events = null)

                    beforeEach {
                        every { eventJpaRepository.findByIdAndAccount(1L, account) } returns null
                    }

                    it("ExpectedException이 발생하고 검증을 트리거하지 않아야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyEventService.execute(1L, reqDto)
                            }
                        ex.message shouldBe "Event를 찾을 수 없습니다."
                        verify(exactly = 0) { applicationEventPublisher.publishEvent(any<EventVerificationRequested>()) }
                    }
                }

                context("targetUrl을 새 값으로 수정할 때") {
                    val reqDto = ModifyEventReqDto(targetUrl = "https://new.example.com", events = null)

                    beforeEach {
                        every { eventJpaRepository.findByIdAndAccount(1L, account) } returns existingEvent()
                    }

                    it("targetUrl이 변경되고 events는 유지되며 상태가 PENDING으로 초기화되어야 한다") {
                        val result = modifyEventService.execute(1L, reqDto)

                        result.targetUrl shouldBe "https://new.example.com"
                        result.events shouldBe setOf(EventType.CLUB_UPDATED)
                        result.verificationStatus shouldBe EventVerificationStatus.PENDING
                    }

                    it("비동기 URL 검증을 트리거해야 한다") {
                        modifyEventService.execute(1L, reqDto)

                        verify(exactly = 1) { applicationEventPublisher.publishEvent(EventVerificationRequested(1L)) }
                    }
                }

                context("targetUrl을 기존과 동일한 값으로 수정할 때") {
                    val reqDto =
                        ModifyEventReqDto(targetUrl = "https://old.example.com/event", events = null)

                    beforeEach {
                        every { eventJpaRepository.findByIdAndAccount(1L, account) } returns existingEvent()
                    }

                    it("상태를 유지하고 검증을 트리거하지 않아야 한다") {
                        val result = modifyEventService.execute(1L, reqDto)

                        result.verificationStatus shouldBe EventVerificationStatus.VERIFIED
                        verify(exactly = 0) { applicationEventPublisher.publishEvent(any<EventVerificationRequested>()) }
                    }
                }

                context("검증에 실패한 Event를 동일한 targetUrl로 재시도할 때") {
                    val reqDto =
                        ModifyEventReqDto(targetUrl = "https://old.example.com/event", events = null)

                    beforeEach {
                        every { eventJpaRepository.findByIdAndAccount(1L, account) } returns
                            existingEvent(EventVerificationStatus.FAILED)
                    }

                    it("상태를 PENDING으로 초기화하고 검증을 다시 트리거해야 한다") {
                        val result = modifyEventService.execute(1L, reqDto)

                        result.verificationStatus shouldBe EventVerificationStatus.PENDING
                        verify(exactly = 1) { applicationEventPublisher.publishEvent(EventVerificationRequested(1L)) }
                    }
                }

                context("events만 수정할 때") {
                    val reqDto =
                        ModifyEventReqDto(targetUrl = null, events = setOf(EventType.PROJECT_UPDATED))

                    beforeEach {
                        every { eventJpaRepository.findByIdAndAccount(1L, account) } returns existingEvent()
                    }

                    it("events가 교체되고 targetUrl은 유지되며 검증을 트리거하지 않아야 한다") {
                        val result = modifyEventService.execute(1L, reqDto)

                        result.targetUrl shouldBe "https://old.example.com/event"
                        result.events shouldBe setOf(EventType.PROJECT_UPDATED)
                        verify(exactly = 0) { applicationEventPublisher.publishEvent(any<EventVerificationRequested>()) }
                    }
                }
            }
        }
    })
