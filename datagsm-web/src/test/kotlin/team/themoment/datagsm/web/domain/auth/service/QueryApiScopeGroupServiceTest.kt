package team.themoment.datagsm.web.domain.auth.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.web.domain.auth.service.impl.QueryApiScopeGroupServiceImpl

class QueryApiScopeGroupServiceTest :
    DescribeSpec({

        val queryApiScopeGroupService = QueryApiScopeGroupServiceImpl()

        describe("QueryApiScopeGroupService 클래스의") {
            describe("execute 메서드는") {
                context("USER 역할로 조회할 때") {
                    it("USER가 사용 가능한 스코프 목록을 카테고리별로 그룹핑하여 반환해야 한다") {
                        val result = queryApiScopeGroupService.execute(AccountRole.USER)

                        result shouldNotBe null
                        result.list.size shouldNotBe 0
                        result.list.all { it.scopes.isNotEmpty() } shouldBe true
                    }
                }

                context("ADMIN 역할로 조회할 때") {
                    it("ADMIN이 사용 가능한 스코프 목록을 카테고리별로 그룹핑하여 반환해야 한다") {
                        val result = queryApiScopeGroupService.execute(AccountRole.ADMIN)

                        result shouldNotBe null
                        result.list.size shouldNotBe 0
                        result.list.all { it.scopes.isNotEmpty() } shouldBe true
                    }
                }
            }
        }
    })
