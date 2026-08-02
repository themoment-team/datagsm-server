package team.themoment.datagsm.common.domain.event.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.springframework.web.client.ResourceAccessException
import team.themoment.datagsm.common.domain.event.entity.EventJpaEntity
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.entity.constant.EventVerificationStatus
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.common.domain.event.service.impl.EventPublisherImpl

class EventPublisherTest :
    DescribeSpec({

        lateinit var eventJpaRepository: EventJpaRepository
        lateinit var eventSender: EventSender
        lateinit var eventPublisher: EventPublisher

        val payloadData = mapOf("studentId" to 1)

        fun target(
            targetId: Long,
            url: String,
        ) = EventJpaEntity().apply {
            id = targetId
            targetUrl = url
            secret = "secret-$targetId"
        }

        fun givenTargets(vararg targets: EventJpaEntity) {
            every {
                eventJpaRepository.findAllByEventsContainsAndIsActiveTrueAndVerificationStatus(
                    EventType.STUDENT_UPDATED,
                    EventVerificationStatus.VERIFIED,
                )
            } returns targets.toList()
        }

        beforeEach {
            eventJpaRepository = mockk()
            eventSender = mockk()
            eventPublisher = EventPublisherImpl(eventJpaRepository, eventSender)
        }

        describe("EventPublisher 클래스의") {
            describe("dispatch 메서드는") {
                context("전송 대상이 없을 때") {
                    beforeEach {
                        givenTargets()
                    }

                    it("전송을 시도하지 않아야 한다") {
                        eventPublisher.dispatch(EventType.STUDENT_UPDATED, payloadData)

                        verify(exactly = 0) { eventSender.send(any(), any(), any()) }
                    }
                }

                context("소비자 쪽 문제로 전송이 실패할 때") {
                    beforeEach {
                        givenTargets(target(1L, "https://first.example.com"), target(2L, "https://second.example.com"))
                        every {
                            eventSender.send("https://first.example.com", any(), any())
                        } throws ResourceAccessException("Connection timed out")
                        every { eventSender.send("https://second.example.com", any(), any()) } just Runs
                    }

                    it("예외를 던지지 않고 나머지 대상에 계속 전송해야 한다") {
                        shouldNotThrowAny {
                            eventPublisher.dispatch(EventType.STUDENT_UPDATED, payloadData)
                        }

                        verify(exactly = 1) { eventSender.send("https://second.example.com", any(), any()) }
                    }
                }

                context("우리 쪽 문제로 전송이 실패할 때") {
                    beforeEach {
                        givenTargets(target(1L, "https://first.example.com"), target(2L, "https://second.example.com"))
                        every {
                            eventSender.send("https://first.example.com", any(), any())
                        } throws IllegalStateException("Failed to compute signature")
                        every { eventSender.send("https://second.example.com", any(), any()) } just Runs
                    }

                    it("나머지 대상에 전송을 마친 뒤 예외를 전파해야 한다") {
                        shouldThrow<IllegalStateException> {
                            eventPublisher.dispatch(EventType.STUDENT_UPDATED, payloadData)
                        }

                        verify(exactly = 1) { eventSender.send("https://second.example.com", any(), any()) }
                    }
                }

                context("우리 쪽 문제가 여러 대상에서 발생할 때") {
                    beforeEach {
                        givenTargets(target(1L, "https://first.example.com"), target(2L, "https://second.example.com"))
                        every {
                            eventSender.send("https://first.example.com", any(), any())
                        } throws IllegalStateException("First failure")
                        every {
                            eventSender.send("https://second.example.com", any(), any())
                        } throws IllegalStateException("Second failure")
                    }

                    it("첫 예외에 나머지 예외를 suppressed로 붙여 전파해야 한다") {
                        val thrown =
                            shouldThrow<IllegalStateException> {
                                eventPublisher.dispatch(EventType.STUDENT_UPDATED, payloadData)
                            }

                        thrown.message shouldBe "First failure"
                        thrown.suppressed.size shouldBe 1
                        thrown.suppressed
                            .first()
                            .message shouldBe "Second failure"
                    }
                }

                context("전송 대상 조회가 실패할 때") {
                    beforeEach {
                        every {
                            eventJpaRepository.findAllByEventsContainsAndIsActiveTrueAndVerificationStatus(
                                EventType.STUDENT_UPDATED,
                                EventVerificationStatus.VERIFIED,
                            )
                        } throws IllegalStateException("Database is unreachable")
                    }

                    it("예외를 전파해야 한다") {
                        shouldThrow<IllegalStateException> {
                            eventPublisher.dispatch(EventType.STUDENT_UPDATED, payloadData)
                        }

                        verify(exactly = 0) { eventSender.send(any(), any(), any()) }
                    }
                }
            }
        }
    })
