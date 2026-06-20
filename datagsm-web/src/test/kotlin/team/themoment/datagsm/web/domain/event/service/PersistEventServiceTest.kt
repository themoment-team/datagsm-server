package team.themoment.datagsm.web.domain.event.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.event.dto.request.CreateEventReqDto
import team.themoment.datagsm.common.domain.event.dto.request.ModifyEventReqDto
import team.themoment.datagsm.common.domain.event.entity.EventJpaEntity
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.web.domain.event.service.impl.PersistEventServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime

class PersistEventServiceTest :
    DescribeSpec({

        lateinit var eventJpaRepository: EventJpaRepository
        lateinit var persistEventService: PersistEventService
        lateinit var account: AccountJpaEntity

        fun existingEvent() =
            EventJpaEntity().apply {
                id = 1L
                targetUrl = "https://old.example.com/event"
                events = mutableSetOf(EventType.CLUB_CREATED)
                this.account = account
                createdAt = LocalDateTime.now()
            }

        beforeEach {
            eventJpaRepository = mockk()
            account = mockk()
            persistEventService = PersistEventServiceImpl(eventJpaRepository)
        }

        describe("PersistEventService 클래스의") {
            describe("persistCreate 메서드는") {
                val secret = "a".repeat(64)
                val reqDto =
                    CreateEventReqDto(
                        targetUrl = "https://example.com/event",
                        events = setOf(EventType.STUDENT_GRADUATED),
                    )

                context("트랜잭션 진입 시점에 최대 개수(10개)에 도달했을 때") {
                    beforeEach {
                        every { eventJpaRepository.countByAccount(account) } returns 10L
                    }

                    it("ExpectedException이 발생하고 저장하지 않아야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                persistEventService.persistCreate(account, reqDto, secret)
                            }
                        ex.message shouldBe "Event는 최대 10개까지 등록할 수 있습니다."
                        verify(exactly = 0) { eventJpaRepository.save(any()) }
                    }
                }

                context("유효한 요청으로 Event를 저장할 때") {
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

                    it("Event를 저장하고 secret을 포함한 응답을 반환해야 한다") {
                        val result = persistEventService.persistCreate(account, reqDto, secret)

                        result.id shouldBe 1L
                        result.targetUrl shouldBe reqDto.targetUrl
                        result.events shouldBe reqDto.events
                        result.secret shouldBe secret
                        verify(exactly = 1) { eventJpaRepository.save(any()) }
                    }
                }
            }

            describe("persistModify 메서드는") {
                context("Event가 존재하지 않을 때") {
                    val reqDto = ModifyEventReqDto(targetUrl = "https://new.example.com", events = null)

                    beforeEach {
                        every { eventJpaRepository.findByIdAndAccount(1L, account) } returns null
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                persistEventService.persistModify(account, 1L, reqDto)
                            }
                        ex.message shouldBe "Event를 찾을 수 없습니다."
                    }
                }

                context("targetUrl만 수정할 때") {
                    val reqDto = ModifyEventReqDto(targetUrl = "https://new.example.com", events = null)

                    beforeEach {
                        every { eventJpaRepository.findByIdAndAccount(1L, account) } returns existingEvent()
                    }

                    it("targetUrl이 변경되고 events는 유지되어야 한다") {
                        val result = persistEventService.persistModify(account, 1L, reqDto)

                        result.targetUrl shouldBe "https://new.example.com"
                        result.events shouldBe setOf(EventType.CLUB_CREATED)
                    }
                }

                context("events만 수정할 때") {
                    val reqDto =
                        ModifyEventReqDto(targetUrl = null, events = setOf(EventType.PROJECT_CREATED))

                    beforeEach {
                        every { eventJpaRepository.findByIdAndAccount(1L, account) } returns existingEvent()
                    }

                    it("events가 교체되고 targetUrl은 유지되어야 한다") {
                        val result = persistEventService.persistModify(account, 1L, reqDto)

                        result.targetUrl shouldBe "https://old.example.com/event"
                        result.events shouldBe setOf(EventType.PROJECT_CREATED)
                    }
                }
            }
        }
    })
