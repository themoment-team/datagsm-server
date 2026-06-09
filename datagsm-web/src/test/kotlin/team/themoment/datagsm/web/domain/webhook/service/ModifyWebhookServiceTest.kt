package team.themoment.datagsm.web.domain.webhook.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.webhook.dto.request.ModifyWebhookReqDto
import team.themoment.datagsm.common.domain.webhook.entity.WebhookJpaEntity
import team.themoment.datagsm.common.domain.webhook.entity.constant.WebhookEvent
import team.themoment.datagsm.common.domain.webhook.repository.WebhookJpaRepository
import team.themoment.datagsm.common.domain.webhook.validator.WebhookUrlValidator
import team.themoment.datagsm.web.domain.webhook.service.impl.ModifyWebhookServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime

class ModifyWebhookServiceTest :
    DescribeSpec({

        lateinit var webhookJpaRepository: WebhookJpaRepository
        lateinit var currentUserProvider: CurrentUserProvider
        lateinit var modifyWebhookService: ModifyWebhookService
        lateinit var account: AccountJpaEntity

        fun existingWebhook() =
            WebhookJpaEntity().apply {
                id = 1L
                targetUrl = "https://old.example.com/webhook"
                events = mutableSetOf(WebhookEvent.CLUB_CREATED)
                this.account = account
                createdAt = LocalDateTime.now()
            }

        beforeEach {
            webhookJpaRepository = mockk()
            currentUserProvider = mockk()
            account = mockk()
            every { currentUserProvider.getCurrentAccount() } returns account
            modifyWebhookService = ModifyWebhookServiceImpl(webhookJpaRepository, currentUserProvider)
            mockkObject(WebhookUrlValidator)
            every { WebhookUrlValidator.isPrivateOrLocalUrl(any()) } returns false
        }

        afterEach {
            unmockkObject(WebhookUrlValidator)
        }

        describe("ModifyWebhookService 클래스의") {
            describe("execute 메서드는") {
                context("Webhook이 존재하지 않을 때") {
                    val reqDto = ModifyWebhookReqDto(targetUrl = "https://new.example.com", events = null)

                    beforeEach {
                        every { webhookJpaRepository.findByIdAndAccount(1L, account) } returns null
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyWebhookService.execute(1L, reqDto)
                            }
                        ex.message shouldBe "Webhook을 찾을 수 없습니다."
                    }
                }

                context("내부 네트워크 URL로 수정하려 할 때") {
                    val reqDto = ModifyWebhookReqDto(targetUrl = "http://localhost", events = null)

                    beforeEach {
                        every { webhookJpaRepository.findByIdAndAccount(1L, account) } returns existingWebhook()
                        every { WebhookUrlValidator.isPrivateOrLocalUrl(any()) } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                modifyWebhookService.execute(1L, reqDto)
                            }
                        ex.message shouldBe "내부 네트워크 URL은 Webhook 수신 URL로 등록할 수 없습니다."
                    }
                }

                context("targetUrl만 수정할 때") {
                    val reqDto = ModifyWebhookReqDto(targetUrl = "https://new.example.com", events = null)

                    beforeEach {
                        every { webhookJpaRepository.findByIdAndAccount(1L, account) } returns existingWebhook()
                    }

                    it("targetUrl이 변경되고 events는 유지되어야 한다") {
                        val result = modifyWebhookService.execute(1L, reqDto)

                        result.targetUrl shouldBe "https://new.example.com"
                        result.events shouldBe setOf(WebhookEvent.CLUB_CREATED)
                    }
                }

                context("events만 수정할 때") {
                    val reqDto =
                        ModifyWebhookReqDto(targetUrl = null, events = setOf(WebhookEvent.PROJECT_CREATED))

                    beforeEach {
                        every { webhookJpaRepository.findByIdAndAccount(1L, account) } returns existingWebhook()
                    }

                    it("events가 교체되고 targetUrl은 유지되어야 한다") {
                        val result = modifyWebhookService.execute(1L, reqDto)

                        result.targetUrl shouldBe "https://old.example.com/webhook"
                        result.events shouldBe setOf(WebhookEvent.PROJECT_CREATED)
                    }
                }
            }
        }
    })
