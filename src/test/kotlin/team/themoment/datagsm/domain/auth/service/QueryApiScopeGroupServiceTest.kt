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

                        result.data shouldHaveSize 4 // student, club, project, neis

                        val studentGroup = result.data.find { it.title == "student:*" }
                        studentGroup shouldNotBe null
                        studentGroup!!.description shouldBe "학생 정보 모든 권한"
                        studentGroup.scopes shouldHaveSize 1
                        studentGroup.scopes[0].scope shouldBe "student:read"
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

                        val adminGroup = result.data.find { it.title == "admin:*" }
                        adminGroup shouldNotBe null
                        adminGroup!!.scopes shouldHaveSize 2 // admin:apikey, admin:excel
                    }

                    it("student 그룹에는 read와 write 스코프가 포함된다") {
                        val result = queryApiScopeGroupService.execute(AccountRole.ADMIN)

                        val studentGroup = result.data.find { it.title == "student:*" }
                        studentGroup shouldNotBe null
                        studentGroup!!.scopes shouldHaveSize 2
                        studentGroup.scopes.map { it.scope } shouldContainAll listOf("student:read", "student:write")
                    }

                    it("각 카테고리의 *:* 스코프는 scopes 리스트에서 제외된다") {
                        val result = queryApiScopeGroupService.execute(AccountRole.ADMIN)

                        result.data.forEach { group ->
                            group.scopes.none { it.scope.endsWith(":*") } shouldBe true
                        }
                    }
                }
            }
        }
    })
