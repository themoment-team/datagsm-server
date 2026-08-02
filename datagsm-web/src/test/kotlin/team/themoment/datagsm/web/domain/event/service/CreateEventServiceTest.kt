package team.themoment.datagsm.web.domain.event.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.event.dto.request.CreateEventReqDto
import team.themoment.datagsm.common.domain.event.entity.EventJpaEntity
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.entity.constant.EventVerificationStatus
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.web.domain.event.dto.internal.EventVerificationRequested
import team.themoment.datagsm.web.domain.event.service.impl.CreateEventServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime

class CreateEventServiceTest :
    DescribeSpec({

        lateinit var eventJpaRepository: EventJpaRepository
        lateinit var currentUserProvider: CurrentUserProvider
        lateinit var applicationEventPublisher: ApplicationEventPublisher
        lateinit var createEventService: CreateEventService
        lateinit var account: AccountJpaEntity

        beforeEach {
            eventJpaRepository = mockk()
            currentUserProvider = mockk()
            applicationEventPublisher = mockk(relaxed = true)
            account = mockk()
            every { currentUserProvider.getCurrentAccount() } returns account
            createEventService =
                CreateEventServiceImpl(eventJpaRepository, currentUserProvider, applicationEventPublisher)
        }

        describe("CreateEventService 클래스의") {
            describe("execute 메서드는") {
                val reqDto =
                    CreateEventReqDto(
                        targetUrl = "https://example.com/event",
                        events = setOf(EventType.STUDENT_UPDATED),
                    )

                context("등록된 Event가 최대 개수(10개)에 도달했을 때") {
                    beforeEach {
                        every { eventJpaRepository.countByAccount(account) } returns 10L
                    }

                    it("ExpectedException이 발생하고 검증을 트리거하지 않아야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                createEventService.execute(reqDto)
                            }
                        ex.message shouldBe "Event는 최대 10개까지 등록할 수 있습니다."
                        verify(exactly = 0) { applicationEventPublisher.publishEvent(any<EventVerificationRequested>()) }
                    }
                }

                context("유효한 요청으로 Event를 등록할 때") {
                    val savedSlot = slot<EventJpaEntity>()

                    beforeEach {
                        every { eventJpaRepository.countByAccount(account) } returns 0L
                        every { eventJpaRepository.save(capture(savedSlot)) } answers {
                            savedSlot.captured.apply {
                                id = 1L
                                createdAt = LocalDateTime.now()
                            }
                        }
                    }

                    it("Event를 PENDING 상태로 저장하고 secret을 포함한 응답을 반환해야 한다") {
                        val result = createEventService.execute(reqDto)

                        result.id shouldBe 1L
                        result.targetUrl shouldBe reqDto.targetUrl
                        result.events shouldBe reqDto.events
                        result.verificationStatus shouldBe EventVerificationStatus.PENDING
                        result.secret.length shouldBe 64
                        verify(exactly = 1) { eventJpaRepository.save(any()) }
                    }

                    it("저장 후 비동기 URL 검증을 트리거해야 한다") {
                        createEventService.execute(reqDto)

                        verify(exactly = 1) { applicationEventPublisher.publishEvent(EventVerificationRequested(1L)) }
                    }
                }
            }
        }
    })
