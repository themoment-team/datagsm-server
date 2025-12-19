package team.themoment.datagsm.domain.auth.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import team.themoment.datagsm.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.domain.auth.service.impl.QueryApiScopeGroupServiceImpl

class QueryApiScopeGroupServiceTest :
    DescribeSpec({

        val queryApiScopeGroupService = QueryApiScopeGroupServiceImpl()

        describe("QueryApiScopeGroupService 클래스의") {
            describe("execute 메서드는") {

                context("USER 역할로 조회할 때") {
                    it("USER가 사용 가능한 스코프만 카테고리별로 그룹핑하여 반환한다") {
                        val result = queryApiScopeGroupService.execute(AccountRole.USER)

                        result.data shouldHaveSize 4

                        val studentGroup = result.data.find { it.title == "학생" }
                        studentGroup shouldNotBe null
                        studentGroup!!.scopes shouldHaveSize 1
                        studentGroup.scopes[0].scope shouldBe "student:read"
                    }

                    it("와일드카드 스코프는 scopes 리스트에 포함된다") {
                        val result = queryApiScopeGroupService.execute(AccountRole.USER)

                        val hasWildcardScope =
                            result.data.any { group ->
                                group.scopes.any { it.scope.endsWith(":*") }
                            }
                        hasWildcardScope shouldBe false // USER는 와일드카드 스코프가 없음
                    }

                    it("auth:manage 스코프는 포함되지 않는다") {
                        val result = queryApiScopeGroupService.execute(AccountRole.USER)

                        val authScopes = result.data.flatMap { it.scopes }.filter { it.scope.startsWith("auth:") }
                        authScopes shouldHaveSize 0
                    }
                }

                context("ADMIN 역할로 조회할 때") {
                    it("USER와 ADMIN이 사용 가능한 모든 스코프를 카테고리별로 그룹핑하여 반환한다") {
                        val result = queryApiScopeGroupService.execute(AccountRole.ADMIN)

                        result.data shouldHaveSize 5 // student, club, project, neis, admin

                        val adminGroup = result.data.find { it.title == "관리자" }
                        adminGroup shouldNotBe null
                        adminGroup!!.scopes shouldHaveSize 3 // admin:*, admin:apikey, admin:excel
                    }

                    it("student 그룹에는 와일드카드, read, write 스코프가 포함된다") {
                        val result = queryApiScopeGroupService.execute(AccountRole.ADMIN)

                        val studentGroup = result.data.find { it.title == "학생" }
                        studentGroup shouldNotBe null
                        studentGroup!!.scopes shouldHaveSize 3 // student:*, student:read, student:write
                        studentGroup.scopes.map { it.scope } shouldContainAll
                            listOf(
                                "student:*",
                                "student:read",
                                "student:write",
                            )
                    }

                    it("와일드카드 스코프가 scopes 리스트에 포함된다") {
                        val result = queryApiScopeGroupService.execute(AccountRole.ADMIN)

                        result.data.forEach { group ->
                            val hasWildcard = group.scopes.any { it.scope.endsWith(":*") }
                            hasWildcard shouldBe true
                        }
                    }
                }
            }
        }
    })
