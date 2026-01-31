package team.themoment.datagsm.resource.global.security.validator

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ScopeValidatorTest :
    DescribeSpec({

        describe("ScopeValidator의") {
            describe("hasScope 메서드는") {
                context("정확한 scope가 있을 때") {
                    val userScopes = setOf("student:read", "club:write")

                    it("student:read 요청 시 true를 반환해야 한다") {
                        ScopeValidator.hasScope(userScopes, "student:read") shouldBe true
                    }

                    it("club:write 요청 시 true를 반환해야 한다") {
                        ScopeValidator.hasScope(userScopes, "club:write") shouldBe true
                    }
                }

                context("와일드카드 scope가 있을 때") {
                    val userScopes = setOf("student:*", "club:read")

                    it("student:read 요청 시 true를 반환해야 한다") {
                        ScopeValidator.hasScope(userScopes, "student:read") shouldBe true
                    }

                    it("student:write 요청 시 true를 반환해야 한다") {
                        ScopeValidator.hasScope(userScopes, "student:write") shouldBe true
                    }

                    it("student로 시작하는 모든 scope에 대해 true를 반환해야 한다") {
                        ScopeValidator.hasScope(userScopes, "student:delete") shouldBe true
                        ScopeValidator.hasScope(userScopes, "student:custom") shouldBe true
                    }

                    it("club:read 요청 시 true를 반환해야 한다") {
                        ScopeValidator.hasScope(userScopes, "club:read") shouldBe true
                    }

                    it("club:write 요청 시 false를 반환해야 한다") {
                        ScopeValidator.hasScope(userScopes, "club:write") shouldBe false
                    }
                }

                context("scope가 없을 때") {
                    val userScopes = setOf("student:read")

                    it("club:read 요청 시 false를 반환해야 한다") {
                        ScopeValidator.hasScope(userScopes, "club:read") shouldBe false
                    }

                    it("student:write 요청 시 false를 반환해야 한다") {
                        ScopeValidator.hasScope(userScopes, "student:write") shouldBe false
                    }
                }

                context("빈 scope 집합일 때") {
                    val userScopes = emptySet<String>()

                    it("모든 요청에 대해 false를 반환해야 한다") {
                        ScopeValidator.hasScope(userScopes, "student:read") shouldBe false
                        ScopeValidator.hasScope(userScopes, "club:write") shouldBe false
                    }
                }
            }

            describe("hasAnyScope 메서드는") {
                context("요청된 scope 중 하나라도 있을 때") {
                    val userScopes = setOf("student:read", "club:write")
                    val requiredScopes = setOf("student:read", "project:read")

                    it("true를 반환해야 한다") {
                        ScopeValidator.hasAnyScope(userScopes, requiredScopes) shouldBe true
                    }
                }

                context("와일드카드로 커버되는 scope가 있을 때") {
                    val userScopes = setOf("student:*")
                    val requiredScopes = setOf("student:read", "student:write")

                    it("true를 반환해야 한다") {
                        ScopeValidator.hasAnyScope(userScopes, requiredScopes) shouldBe true
                    }
                }

                context("요청된 scope가 하나도 없을 때") {
                    val userScopes = setOf("student:read")
                    val requiredScopes = setOf("club:read", "project:read")

                    it("false를 반환해야 한다") {
                        ScopeValidator.hasAnyScope(userScopes, requiredScopes) shouldBe false
                    }
                }

                context("빈 요청 scope 집합일 때") {
                    val userScopes = setOf("student:read")
                    val requiredScopes = emptySet<String>()

                    it("false를 반환해야 한다") {
                        ScopeValidator.hasAnyScope(userScopes, requiredScopes) shouldBe false
                    }
                }
            }

            describe("hasAllScopes 메서드는") {
                context("요청된 모든 scope가 있을 때") {
                    val userScopes = setOf("student:read", "club:write", "project:read")
                    val requiredScopes = setOf("student:read", "club:write")

                    it("true를 반환해야 한다") {
                        ScopeValidator.hasAllScopes(userScopes, requiredScopes) shouldBe true
                    }
                }

                context("와일드카드로 모든 scope를 커버할 때") {
                    val userScopes = setOf("student:*", "club:*")
                    val requiredScopes = setOf("student:read", "student:write", "club:read")

                    it("true를 반환해야 한다") {
                        ScopeValidator.hasAllScopes(userScopes, requiredScopes) shouldBe true
                    }
                }

                context("일부 scope가 없을 때") {
                    val userScopes = setOf("student:read", "club:write")
                    val requiredScopes = setOf("student:read", "project:read")

                    it("false를 반환해야 한다") {
                        ScopeValidator.hasAllScopes(userScopes, requiredScopes) shouldBe false
                    }
                }

                context("와일드카드가 일부만 커버할 때") {
                    val userScopes = setOf("student:*")
                    val requiredScopes = setOf("student:read", "club:read")

                    it("false를 반환해야 한다") {
                        ScopeValidator.hasAllScopes(userScopes, requiredScopes) shouldBe false
                    }
                }

                context("빈 요청 scope 집합일 때") {
                    val userScopes = setOf("student:read")
                    val requiredScopes = emptySet<String>()

                    it("true를 반환해야 한다") {
                        ScopeValidator.hasAllScopes(userScopes, requiredScopes) shouldBe true
                    }
                }
            }
        }
    })
