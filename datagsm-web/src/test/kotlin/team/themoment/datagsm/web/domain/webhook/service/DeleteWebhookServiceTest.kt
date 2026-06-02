package team.themoment.datagsm.web.domain.webhook.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.webhook.entity.WebhookJpaEntity
import team.themoment.datagsm.common.domain.webhook.entity.constant.WebhookEvent
import team.themoment.datagsm.common.domain.webhook.repository.WebhookJpaRepository
import team.themoment.datagsm.web.domain.webhook.service.impl.DeleteWebhookServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime

class DeleteWebhookServiceTest :
    DescribeSpec({

        lateinit var webhookJpaRepository: WebhookJpaRepository
        lateinit var currentUserProvider: CurrentUserProvider
        lateinit var deleteWebhookService: DeleteWebhookService
        lateinit var account: AccountJpaEntity

        beforeEach {
            webhookJpaRepository = mockk()
            currentUserProvider = mockk()
            account = mockk()
            every { currentUserProvider.getCurrentAccount() } returns account
            deleteWebhookService = DeleteWebhookServiceImpl(webhookJpaRepository, currentUserProvider)
        }

        describe("DeleteWebhookService 클래스의") {
            describe("execute 메서드는") {
                context("Webhook이 존재하지 않을 때") {
                    beforeEach {
                        every { webhookJpaRepository.findByIdAndAccount(1L, account) } returns null
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                deleteWebhookService.execute(1L)
                            }
                        ex.message shouldBe "Webhook을 찾을 수 없습니다."
                    }
                }

                context("Webhook이 존재할 때") {
                    val webhook =
                        WebhookJpaEntity().apply {
                            id = 1L
                            targetUrl = "https://example.com/webhook"
                            events = mutableSetOf(WebhookEvent.CLUB_DELETED)
                            createdAt = LocalDateTime.now()
                        }

                    beforeEach {
                        every { webhookJpaRepository.findByIdAndAccount(1L, account) } returns webhook
                        justRun { webhookJpaRepository.delete(webhook) }
                    }

                    it("Webhook을 삭제해야 한다") {
                        deleteWebhookService.execute(1L)

                        verify(exactly = 1) { webhookJpaRepository.delete(webhook) }
                    }
                }
            }
        }
    })
