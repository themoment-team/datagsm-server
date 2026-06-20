package team.themoment.datagsm.web.domain.event.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.event.dto.request.ModifyEventReqDto
import team.themoment.datagsm.common.domain.event.dto.response.EventResDto
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.validator.EventUrlValidator
import team.themoment.datagsm.web.domain.event.service.PersistEventService
import team.themoment.datagsm.web.domain.event.service.impl.ModifyEventServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime

class ModifyEventServiceTest :
    DescribeSpec({

        lateinit var currentUserProvider: CurrentUserProvider
        lateinit var persistEventService: PersistEventService
        lateinit var modifyEventService: ModifyEventService
        lateinit var account: AccountJpaEntity

        beforeEach {
            currentUserProvider = mockk()
            persistEventService = mockk()
            account = mockk()
            every { currentUserProvider.getCurrentAccount() } returns account
            modifyEventService = ModifyEventServiceImpl(currentUserProvider, persistEventService)
            mockkObject(EventUrlValidator)
            every { EventUrlValidator.isPrivateOrLocalUrl(any()) } returns false
        }

        afterEach {
            unmockkObject(EventUrlValidator)
        }

        describe("ModifyEventService 클래스의") {
            describe("execute 메서드는") {
                context("내부 네트워크 URL로 수정하려 할 때") {
                    val reqDto = ModifyEventReqDto(targetUrl = "http://localhost", events = null)

                    beforeEach {
                        every { EventUrlValidator.isPrivateOrLocalUrl(any()) } returns true
                    }

                    it("ExpectedException이 발생하고 persist를 호출하지 않아야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyEventService.execute(1L, reqDto)
                            }
                        ex.message shouldBe "내부 네트워크 URL은 Event 수신 URL로 등록할 수 없습니다."
                        verify(exactly = 0) { persistEventService.persistModify(any(), any(), any()) }
                    }
                }

                context("유효한 요청으로 Event를 수정할 때") {
                    val reqDto = ModifyEventReqDto(targetUrl = "https://new.example.com", events = null)
                    val resDto =
                        EventResDto(
                            id = 1L,
                            targetUrl = "https://new.example.com",
                            events = setOf(EventType.CLUB_CREATED),
                            isActive = true,
                            createdAt = LocalDateTime.now(),
                        )

                    beforeEach {
                        every { persistEventService.persistModify(account, 1L, reqDto) } returns resDto
                    }

                    it("URL 검증 후 persist 결과를 반환해야 한다") {
                        val result = modifyEventService.execute(1L, reqDto)

                        result shouldBe resDto
                        verify(exactly = 1) { persistEventService.persistModify(account, 1L, reqDto) }
                    }
                }

                context("targetUrl 없이 events만 수정할 때") {
                    val reqDto =
                        ModifyEventReqDto(targetUrl = null, events = setOf(EventType.PROJECT_CREATED))
                    val resDto =
                        EventResDto(
                            id = 1L,
                            targetUrl = "https://old.example.com/event",
                            events = setOf(EventType.PROJECT_CREATED),
                            isActive = true,
                            createdAt = LocalDateTime.now(),
                        )

                    beforeEach {
                        every { persistEventService.persistModify(account, 1L, reqDto) } returns resDto
                    }

                    it("URL 검증 없이 persist 결과를 반환해야 한다") {
                        val result = modifyEventService.execute(1L, reqDto)

                        result shouldBe resDto
                        verify(exactly = 0) { EventUrlValidator.isPrivateOrLocalUrl(any()) }
                        verify(exactly = 1) { persistEventService.persistModify(account, 1L, reqDto) }
                    }
                }
            }
        }
    })
