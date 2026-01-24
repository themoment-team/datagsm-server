package team.themoment.datagsm.web.domain.client.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import team.themoment.datagsm.web.domain.client.service.impl.GetAvailableOauthScopesServiceImpl

class GetAvailableOauthScopesServiceTest :
    DescribeSpec({

        val getAvailableOauthScopesService = GetAvailableOauthScopesServiceImpl()

        describe("GetAvailableOauthScopesService 클래스의") {
            describe("execute 메서드는") {
                context("호출할 때") {
                    it("ApiScopeGroupListResDto 형식으로 반환해야 한다") {
                        val result = getAvailableOauthScopesService.execute()

                        result shouldNotBe null
                        result.list shouldNotBe null
                    }

                    it("비어있지 않은 리스트를 반환해야 한다") {
                        val result = getAvailableOauthScopesService.execute()

                        result.list.isNotEmpty() shouldBe true
                    }

                    it("각 그룹의 title과 scopes가 올바르게 설정되어야 한다") {
                        val result = getAvailableOauthScopesService.execute()

                        result.list.all { group ->
                            group.title.isNotBlank() && group.scopes.isNotEmpty()
                        } shouldBe true
                    }

                    it("'사용자' 카테고리가 포함되어야 한다") {
                        val result = getAvailableOauthScopesService.execute()

                        val categoryTitles = result.list.map { it.title }
                        categoryTitles.contains("사용자") shouldBe true
                    }

                    it("'self:read' 스코프가 포함되어야 한다") {
                        val result = getAvailableOauthScopesService.execute()

                        val allScopes = result.list.flatMap { it.scopes.map { scope -> scope.scope } }
                        allScopes.contains("self:read") shouldBe true
                    }

                    it("'self:read' 스코프의 description이 올바르게 설정되어야 한다") {
                        val result = getAvailableOauthScopesService.execute()

                        val selfReadScope =
                            result.list
                                .flatMap { it.scopes }
                                .find { it.scope == "self:read" }

                        selfReadScope shouldNotBe null
                        selfReadScope?.description shouldBe "내 정보 조회"
                    }
                }
            }
        }
    })
