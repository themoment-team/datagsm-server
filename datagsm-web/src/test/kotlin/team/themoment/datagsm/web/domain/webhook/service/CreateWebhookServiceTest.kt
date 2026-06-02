package team.themoment.datagsm.web.domain.webhook.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.webhook.dto.request.CreateWebhookReqDto
import team.themoment.datagsm.common.domain.webhook.entity.WebhookJpaEntity
import team.themoment.datagsm.common.domain.webhook.entity.constant.WebhookEvent
import team.themoment.datagsm.common.domain.webhook.repository.WebhookJpaRepository
import team.themoment.datagsm.common.domain.webhook.validator.WebhookUrlValidator
import team.themoment.datagsm.web.domain.webhook.service.impl.CreateWebhookServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime

class CreateWebhookServiceTest :
    DescribeSpec({

        lateinit var webhookJpaRepository: WebhookJpaRepository
        lateinit var currentUserProvider: CurrentUserProvider
        lateinit var createWebhookService: CreateWebhookService
        lateinit var account: AccountJpaEntity

        beforeEach {
            webhookJpaRepository = mockk()
            currentUserProvider = mockk()
            account = mockk()
            every { currentUserProvider.getCurrentAccount() } returns account
            createWebhookService = CreateWebhookServiceImpl(webhookJpaRepository, currentUserProvider)
            mockkObject(WebhookUrlValidator)
            every { WebhookUrlValidator.isPrivateOrLocalUrl(any()) } returns false
        }

        afterEach {
            unmockkObject(WebhookUrlValidator)
        }

        describe("CreateWebhookService 클래스의") {
            describe("execute 메서드는") {
                val reqDto =
                    CreateWebhookReqDto(
                        targetUrl = "https://example.com/webhook",
                        events = setOf(WebhookEvent.STUDENT_GRADUATED),
                    )

                context("등록된 Webhook이 최대 개수(10개)에 도달했을 때") {
                    beforeEach {
                        every { webhookJpaRepository.countByAccount(account) } returns 10L
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                createWebhookService.execute(reqDto)
                            }
                        ex.message shouldBe "Webhook은 최대 10개까지 등록할 수 있습니다."
                    }
                }

                context("내부 네트워크 URL을 등록하려 할 때") {
                    beforeEach {
                        every { webhookJpaRepository.countByAccount(account) } returns 0L
                        every { WebhookUrlValidator.isPrivateOrLocalUrl(any()) } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                createWebhookService.execute(reqDto)
                            }
                        ex.message shouldBe "내부 네트워크 URL은 Webhook 수신 URL로 등록할 수 없습니다."
                    }
                }

                context("유효한 요청으로 Webhook을 등록할 때") {
                    val savedSlot = slot<WebhookJpaEntity>()

                    beforeEach {
                        every { webhookJpaRepository.countByAccount(account) } returns 0L
                        every { webhookJpaRepository.save(capture(savedSlot)) } answers {
                            savedSlot.captured.apply {
                                id = 1L
                                createdAt = LocalDateTime.now()
                            }
                        }
                    }

                    it("Webhook을 저장하고 secret을 포함한 응답을 반환해야 한다") {
                        val result = createWebhookService.execute(reqDto)

                        result.id shouldBe 1L
                        result.targetUrl shouldBe reqDto.targetUrl
                        result.events shouldBe reqDto.events
                        result.secret.length shouldBe 64
                        verify(exactly = 1) { webhookJpaRepository.save(any()) }
                    }
                }
            }
        }
    })
