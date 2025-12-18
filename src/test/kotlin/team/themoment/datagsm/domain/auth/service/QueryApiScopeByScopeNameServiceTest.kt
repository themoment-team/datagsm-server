package team.themoment.datagsm.domain.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import team.themoment.datagsm.domain.auth.service.impl.QueryApiScopeByScopeNameServiceImpl
import team.themoment.datagsm.global.exception.error.ExpectedException

class QueryApiScopeByScopeNameServiceTest :
    DescribeSpec({

        val queryApiScopeByScopeNameService = QueryApiScopeByScopeNameServiceImpl()

        describe("QueryApiScopeByScopeNameService 클래스의") {
            describe("execute 메서드는") {

                context("존재하는 스코프 이름으로 조회할 때") {
                    it("해당 스코프의 정보를 반환한다") {
                        val result = queryApiScopeByScopeNameService.execute("student:read")

                        result.scope shouldBe "student:read"
                        result.description shouldBe "학생 정보 조회"
                    }

                    it("와일드카드 스코프도 조회할 수 있다") {
                        val result = queryApiScopeByScopeNameService.execute("student:*")

                        result.scope shouldBe "student:*"
                        result.description shouldBe "학생 정보 모든 권한"
                    }

                    it("admin 스코프도 조회할 수 있다") {
                        val result = queryApiScopeByScopeNameService.execute("admin:apikey")

                        result.scope shouldBe "admin:apikey"
                        result.description shouldBe "Admin API 키 생성/갱신"
                    }
                }

                context("존재하지 않는 스코프 이름으로 조회할 때") {
                    it("NOT_FOUND 예외를 발생시킨다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                queryApiScopeByScopeNameService.execute("invalid:scope")
                            }

                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                        exception.message shouldBe "해당 권한 범위 invalid:scope 는 존재하지 않습니다."
                    }

                    it("잘못된 형식의 스코프도 예외를 발생시킨다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                queryApiScopeByScopeNameService.execute("notexist")
                            }

                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                    }
                }
            }
        }
    })
