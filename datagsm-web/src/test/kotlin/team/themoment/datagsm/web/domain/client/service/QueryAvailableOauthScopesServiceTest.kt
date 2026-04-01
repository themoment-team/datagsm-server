package team.themoment.datagsm.web.domain.client.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.entity.OAuthScopeJpaEntity
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.web.domain.client.service.impl.QueryAvailableOauthScopesServiceImpl

class QueryAvailableOauthScopesServiceTest :
    DescribeSpec({

        val mockApplicationJpaRepository = mockk<ApplicationJpaRepository>()
        val queryAvailableOauthScopesService = QueryAvailableOauthScopesServiceImpl(mockApplicationJpaRepository)

        describe("QueryAvailableOauthScopesService 클래스의") {
            describe("execute 메서드는") {
                context("Application이 존재하지 않을 때") {
                    beforeEach {
                        every { mockApplicationJpaRepository.findAllByEager() } returns emptyList()
                    }

                    it("OAuthScopeListResDto 형식으로 반환해야 한다") {
                        val result = queryAvailableOauthScopesService.execute()

                        result shouldNotBe null
                        result.list shouldNotBe null
                    }

                    it("빈 리스트를 반환해야 한다") {
                        val result = queryAvailableOauthScopesService.execute()

                        result.list.isEmpty() shouldBe true
                    }
                }

                context("OAuth 권한 범위가 있는 Application이 존재할 때") {
                    val application =
                        ApplicationJpaEntity().apply {
                            id = "app1"
                            name = "테스트 앱"
                            account = mockk()
                        }
                    val oauthScope =
                        OAuthScopeJpaEntity().apply {
                            scopeName = "data"
                            description = "데이터 조회"
                            this.application = application
                        }
                    application.oauthScopes.add(oauthScope)

                    beforeEach {
                        every { mockApplicationJpaRepository.findAllByEager() } returns listOf(application)
                    }

                    it("OAuth 권한 범위가 반환값에 포함되어야 한다") {
                        val result = queryAvailableOauthScopesService.execute()

                        val allScopes = result.list.map { it.scope }
                        allScopes.contains("app1:data") shouldBe true
                    }

                    it("OAuthScopeResDto에 applicationName이 포함되어야 한다") {
                        val result = queryAvailableOauthScopesService.execute()

                        val scope = result.list.find { it.scope == "app1:data" }
                        scope shouldNotBe null
                        scope?.applicationName shouldBe "테스트 앱"
                    }
                }

                context("OAuth 권한 범위가 없는 Application만 존재할 때") {
                    val emptyApplication =
                        ApplicationJpaEntity().apply {
                            id = "app2"
                            name = "빈 앱"
                            account = mockk()
                        }

                    beforeEach {
                        every { mockApplicationJpaRepository.findAllByEager() } returns listOf(emptyApplication)
                    }

                    it("빈 리스트를 반환해야 한다") {
                        val result = queryAvailableOauthScopesService.execute()

                        result.list.isEmpty() shouldBe true
                    }
                }
            }
        }
    })
