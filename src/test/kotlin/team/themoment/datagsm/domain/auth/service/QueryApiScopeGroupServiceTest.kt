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
                    val result = queryApiScopeGroupService.execute(AccountRole.USER)

                    it("4개의 카테고리(student, club, project, neis)를 반환한다") {
                        result.data shouldHaveSize 4
                    }

                    it("학생 카테고리에는 read 스코프만 포함된다") {
                        val studentGroup = result.data.find { it.title == "학생" }

                        studentGroup shouldNotBe null
                        studentGroup!!.scopes shouldHaveSize 1
                        studentGroup.scopes[0].scope shouldBe "student:read"
                    }

                    it("와일드카드 스코프를 포함하지 않는다") {
                        result.data.all { group ->
                            group.scopes.none { it.scope.endsWith(":*") }
                        } shouldBe true
                    }

                    it("auth:manage 스코프는 제외된다") {
                        result.data
                            .flatMap { it.scopes }
                            .none { it.scope.startsWith("auth:") } shouldBe true
                    }
                }

                context("ADMIN 역할로 조회할 때") {
                    val result = queryApiScopeGroupService.execute(AccountRole.ADMIN)

                    it("5개의 카테고리(student, club, project, neis, admin)를 반환한다") {
                        result.data shouldHaveSize 5
                    }

                    it("관리자 카테고리에는 3개의 스코프(*, apikey, excel)가 포함된다") {
                        val adminGroup = result.data.find { it.title == "관리자" }

                        adminGroup shouldNotBe null
                        adminGroup!!.scopes shouldHaveSize 3
                        adminGroup.scopes.map { it.scope } shouldContainAll
                            listOf("admin:*", "admin:apikey", "admin:excel")
                    }

                    it("학생 카테고리에는 와일드카드, read, write 스코프가 포함된다") {
                        val studentGroup = result.data.find { it.title == "학생" }

                        studentGroup shouldNotBe null
                        studentGroup!!.scopes shouldHaveSize 3
                        studentGroup.scopes.map { it.scope } shouldContainAll
                            listOf("student:*", "student:read", "student:write")
                    }

                    it("모든 카테고리에 와일드카드 스코프가 포함된다") {
                        result.data.all { group ->
                            group.scopes.any { it.scope.endsWith(":*") }
                        } shouldBe true
                    }
                }
            }
        }
    })
