package team.themoment.datagsm.web.domain.event.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import team.themoment.datagsm.common.domain.event.entity.EventJpaEntity
import team.themoment.datagsm.common.domain.event.entity.constant.EventVerificationStatus
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.common.domain.event.validator.EventUrlValidator
import team.themoment.datagsm.web.domain.event.service.impl.VerifyEventServiceImpl
import java.util.Optional

class VerifyEventServiceTest :
    DescribeSpec({

        lateinit var eventJpaRepository: EventJpaRepository
        lateinit var verifyEventService: VerifyEventService

        fun pendingEvent() =
            EventJpaEntity().apply {
                id = 1L
                targetUrl = "https://example.com/event"
                verificationStatus = EventVerificationStatus.PENDING
            }

        beforeEach {
            eventJpaRepository = mockk()
            verifyEventService = VerifyEventServiceImpl(eventJpaRepository)
            mockkObject(EventUrlValidator)
        }

        afterEach {
            unmockkObject(EventUrlValidator)
        }

        describe("VerifyEventService 클래스의") {
            describe("verifyAsync 메서드는") {
                context("공개 URL로 검증에 성공할 때") {
                    val event = pendingEvent()

                    beforeEach {
                        every { eventJpaRepository.findById(1L) } returns Optional.of(event)
                        every { EventUrlValidator.isPrivateOrLocalUrl(any()) } returns false
                    }

                    it("상태를 VERIFIED로 갱신해야 한다") {
                        verifyEventService.verifyAsync(1L)

                        event.verificationStatus shouldBe EventVerificationStatus.VERIFIED
                    }
                }

                context("내부 네트워크 URL로 검증에 실패할 때") {
                    val event = pendingEvent()

                    beforeEach {
                        every { eventJpaRepository.findById(1L) } returns Optional.of(event)
                        every { EventUrlValidator.isPrivateOrLocalUrl(any()) } returns true
                    }

                    it("상태를 FAILED로 갱신해야 한다") {
                        verifyEventService.verifyAsync(1L)

                        event.verificationStatus shouldBe EventVerificationStatus.FAILED
                    }
                }

                context("Event가 존재하지 않을 때") {
                    beforeEach {
                        every { eventJpaRepository.findById(1L) } returns Optional.empty()
                    }

                    it("예외 없이 종료되어야 한다") {
                        verifyEventService.verifyAsync(1L)
                    }
                }
            }
        }
    })
