package team.themoment.datagsm.web.domain.webhook.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.webhook.entity.WebhookJpaEntity
import team.themoment.datagsm.common.domain.webhook.entity.constant.WebhookEvent
import team.themoment.datagsm.common.domain.webhook.repository.WebhookJpaRepository
import team.themoment.datagsm.web.domain.webhook.service.impl.QueryWebhookServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import java.time.LocalDateTime

class QueryWebhookServiceTest :
    DescribeSpec({

        lateinit var webhookJpaRepository: WebhookJpaRepository
        lateinit var currentUserProvider: CurrentUserProvider
        lateinit var queryWebhookService: QueryWebhookService
        lateinit var account: AccountJpaEntity

        beforeEach {
            webhookJpaRepository = mockk()
            currentUserProvider = mockk()
            account = mockk()
            every { currentUserProvider.getCurrentAccount() } returns account
            queryWebhookService = QueryWebhookServiceImpl(webhookJpaRepository, currentUserProvider)
        }

        describe("QueryWebhookService 클래스의") {
            describe("execute 메서드는") {
                context("등록된 Webhook이 있을 때") {
                    beforeEach {
                        val webhook =
                            WebhookJpaEntity().apply {
                                id = 1L
                                targetUrl = "https://example.com/webhook"
                                events = mutableSetOf(WebhookEvent.CLUB_CREATED)
                                this.account = account
                                createdAt = LocalDateTime.now()
                            }
                        every { webhookJpaRepository.findAllByAccount(account) } returns listOf(webhook)
                    }

                    it("Webhook 목록을 반환해야 한다") {
                        val result = queryWebhookService.execute()

                        result.webhooks.size shouldBe 1
                        result.webhooks[0].id shouldBe 1L
                        result.webhooks[0].targetUrl shouldBe "https://example.com/webhook"
                        result.webhooks[0].events shouldBe setOf(WebhookEvent.CLUB_CREATED)
                    }
                }

                context("등록된 Webhook이 없을 때") {
                    beforeEach {
                        every { webhookJpaRepository.findAllByAccount(account) } returns emptyList()
                    }

                    it("빈 목록을 반환해야 한다") {
                        val result = queryWebhookService.execute()

                        result.webhooks.size shouldBe 0
                    }
                }
            }
        }
    })
