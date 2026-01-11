package team.themoment.datagsm.web.domain.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import team.themoment.datagsm.common.domain.account.entity.constant.ApiScope
import team.themoment.datagsm.web.domain.auth.service.impl.QueryApiScopeByScopeNameServiceImpl
import team.themoment.sdk.exception.ExpectedException

class QueryApiScopeByScopeNameServiceTest :
    DescribeSpec({

        val queryApiScopeByScopeNameService = QueryApiScopeByScopeNameServiceImpl()

        describe("QueryApiScopeByScopeNameService 클래스의") {
            describe("execute 메서드는") {
                context("존재하는 스코프 이름으로 조회할 때") {
                    it("해당 스코프의 상세 정보를 반환해야 한다") {
                        val scopeName = ApiScope.STUDENT_READ.scope
                        val result = queryApiScopeByScopeNameService.execute(scopeName)

                        result.scope shouldBe scopeName
                        result.description shouldBe ApiScope.STUDENT_READ.description
                    }
                }

                context("존재하지 않는 스코프 이름으로 조회할 때") {
                    it("ExpectedException이 발생해야 한다") {
                        val invalidScopeName = "invalid:scope"

                        val exception =
                            shouldThrow<ExpectedException> {
                                queryApiScopeByScopeNameService.execute(invalidScopeName)
                            }

                        exception.message shouldBe "해당 권한 범위 $invalidScopeName 는 존재하지 않습니다."
                    }
                }
            }
        }
    })
