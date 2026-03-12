package team.themoment.datagsm.web.domain.client.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.entity.ThirdPartyScopeJpaEntity
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.web.domain.client.service.impl.QueryAvailableOauthScopesServiceImpl

class QueryAvailableOauthScopesServiceTest :
    DescribeSpec({

        val mockApplicationJpaRepository = mockk<ApplicationJpaRepository>()
        val queryAvailableOauthScopesService = QueryAvailableOauthScopesServiceImpl(mockApplicationJpaRepository)

        describe("QueryAvailableOauthScopesService 클래스의") {
            describe("execute 메서드는") {
                context("ThirdPartyScope가 없을 때") {
                    beforeEach {
                        every { mockApplicationJpaRepository.findAllByEager() } returns emptyList()
                    }

                    it("OAuthScopeGroupListResDto 형식으로 반환해야 한다") {
                        val result = queryAvailableOauthScopesService.execute()

                        result shouldNotBe null
                        result.list shouldNotBe null
                    }

                    it("비어있지 않은 리스트를 반환해야 한다") {
                        val result = queryAvailableOauthScopesService.execute()

                        result.list.isNotEmpty() shouldBe true
                    }

                    it("각 그룹의 title과 scopes가 올바르게 설정되어야 한다") {
                        val result = queryAvailableOauthScopesService.execute()

                        result.list.all { group ->
                            group.title.isNotBlank() && group.scopes.isNotEmpty()
                        } shouldBe true
                    }

                    it("'사용자' 카테고리가 포함되어야 한다") {
                        val result = queryAvailableOauthScopesService.execute()

                        val categoryTitles = result.list.map { it.title }
                        categoryTitles.contains("사용자") shouldBe true
                    }

                    it("'self:read' 스코프가 포함되어야 한다") {
                        val result = queryAvailableOauthScopesService.execute()

                        val allScopes = result.list.flatMap { it.scopes.map { scope -> scope.scope } }
                        allScopes.contains("self:read") shouldBe true
                    }

                    it("'self:read' 스코프의 description이 올바르게 설정되어야 한다") {
                        val result = queryAvailableOauthScopesService.execute()

                        val selfReadScope =
                            result.list
                                .flatMap { it.scopes }
                                .find { it.scope == "self:read" }

                        selfReadScope shouldNotBe null
                        selfReadScope?.description shouldBe "내 정보 조회"
                    }
                }

                context("ThirdPartyScope가 있는 Application이 존재할 때") {
                    val application =
                        ApplicationJpaEntity().apply {
                            id = "app1"
                            name = "테스트 앱"
                        }
                    val thirdPartyScope =
                        ThirdPartyScopeJpaEntity().apply {
                            scopeName = "data"
                            description = "데이터 조회"
                            this.application = application
                        }
                    application.thirdPartyScopes.add(thirdPartyScope)

                    beforeEach {
                        every { mockApplicationJpaRepository.findAllByEager() } returns listOf(application)
                    }

                    it("ThirdPartyScope가 반환값에 포함되어야 한다") {
                        val result = queryAvailableOauthScopesService.execute()

                        val allScopes = result.list.flatMap { it.scopes.map { scope -> scope.scope } }
                        allScopes.contains("app1:data") shouldBe true
                    }

                    it("기본 스코프 그룹이 ThirdPartyScope 그룹보다 앞에 위치해야 한다") {
                        val result = queryAvailableOauthScopesService.execute()

                        result.list.first().title shouldBe "사용자"
                    }

                    it("Application 이름이 그룹 title로 설정되어야 한다") {
                        val result = queryAvailableOauthScopesService.execute()

                        val thirdPartyGroup = result.list.find { it.title == "테스트 앱" }
                        thirdPartyGroup shouldNotBe null
                    }
                }

                context("ThirdPartyScope가 없는 Application만 존재할 때") {
                    val emptyApplication =
                        ApplicationJpaEntity().apply {
                            id = "app2"
                            name = "빈 앱"
                        }

                    beforeEach {
                        every { mockApplicationJpaRepository.findAllByEager() } returns listOf(emptyApplication)
                    }

                    it("해당 Application은 그룹에 포함되지 않아야 한다") {
                        val result = queryAvailableOauthScopesService.execute()

                        val groupTitles = result.list.map { it.title }
                        groupTitles.contains("빈 앱") shouldBe false
                    }
                }
            }
        }
    })
