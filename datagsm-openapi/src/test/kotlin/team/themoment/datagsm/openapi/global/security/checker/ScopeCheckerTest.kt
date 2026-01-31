package team.themoment.datagsm.openapi.global.security.checker

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class ScopeCheckerTest :
    DescribeSpec({

        val scopeChecker = ScopeChecker()

        describe("ScopeChecker의") {
            describe("hasScope 메서드는") {
                context("SCOPE_ 접두사가 있는 권한이 있을 때") {
                    val mockAuth =
                        mockk<Authentication> {
                            every { authorities } returns
                                listOf(
                                    GrantedAuthority { "SCOPE_student:read" },
                                    GrantedAuthority { "SCOPE_club:write" },
                                    GrantedAuthority { "ROLE_USER" },
                                )
                        }

                    it("student:read 요청 시 true를 반환해야 한다") {
                        scopeChecker.hasScope(mockAuth, "student:read") shouldBe true
                    }

                    it("club:write 요청 시 true를 반환해야 한다") {
                        scopeChecker.hasScope(mockAuth, "club:write") shouldBe true
                    }

                    it("project:read 요청 시 false를 반환해야 한다") {
                        scopeChecker.hasScope(mockAuth, "project:read") shouldBe false
                    }
                }

                context("와일드카드 scope가 있을 때") {
                    val mockAuth =
                        mockk<Authentication> {
                            every { authorities } returns
                                listOf(
                                    GrantedAuthority { "SCOPE_student:*" },
                                    GrantedAuthority { "ROLE_USER" },
                                )
                        }

                    it("student:read 요청 시 true를 반환해야 한다") {
                        scopeChecker.hasScope(mockAuth, "student:read") shouldBe true
                    }

                    it("student:write 요청 시 true를 반환해야 한다") {
                        scopeChecker.hasScope(mockAuth, "student:write") shouldBe true
                    }

                    it("club:read 요청 시 false를 반환해야 한다") {
                        scopeChecker.hasScope(mockAuth, "club:read") shouldBe false
                    }
                }

                context("SCOPE_ 접두사가 없는 권한만 있을 때") {
                    val mockAuth =
                        mockk<Authentication> {
                            every { authorities } returns
                                listOf(
                                    GrantedAuthority { "ROLE_USER" },
                                    GrantedAuthority { "ROLE_ADMIN" },
                                )
                        }

                    it("모든 scope 요청에 대해 false를 반환해야 한다") {
                        scopeChecker.hasScope(mockAuth, "student:read") shouldBe false
                        scopeChecker.hasScope(mockAuth, "club:write") shouldBe false
                    }
                }

                context("권한이 비어있을 때") {
                    val mockAuth =
                        mockk<Authentication> {
                            every { authorities } returns emptyList()
                        }

                    it("모든 scope 요청에 대해 false를 반환해야 한다") {
                        scopeChecker.hasScope(mockAuth, "student:read") shouldBe false
                    }
                }
            }

            describe("hasAnyScope 메서드는") {
                context("요청된 scope 중 하나라도 있을 때") {
                    val mockAuth =
                        mockk<Authentication> {
                            every { authorities } returns
                                listOf(
                                    GrantedAuthority { "SCOPE_student:read" },
                                    GrantedAuthority { "SCOPE_club:write" },
                                )
                        }

                    it("true를 반환해야 한다") {
                        scopeChecker.hasAnyScope(mockAuth, setOf("student:read", "project:read")) shouldBe true
                    }
                }

                context("와일드카드로 커버되는 scope가 있을 때") {
                    val mockAuth =
                        mockk<Authentication> {
                            every { authorities } returns
                                listOf(
                                    GrantedAuthority { "SCOPE_student:*" },
                                )
                        }

                    it("true를 반환해야 한다") {
                        scopeChecker.hasAnyScope(mockAuth, setOf("student:read", "student:write")) shouldBe true
                    }
                }

                context("요청된 scope가 하나도 없을 때") {
                    val mockAuth =
                        mockk<Authentication> {
                            every { authorities } returns
                                listOf(
                                    GrantedAuthority { "SCOPE_student:read" },
                                )
                        }

                    it("false를 반환해야 한다") {
                        scopeChecker.hasAnyScope(mockAuth, setOf("club:read", "project:read")) shouldBe false
                    }
                }
            }

            describe("hasAllScopes 메서드는") {
                context("요청된 모든 scope가 있을 때") {
                    val mockAuth =
                        mockk<Authentication> {
                            every { authorities } returns
                                listOf(
                                    GrantedAuthority { "SCOPE_student:read" },
                                    GrantedAuthority { "SCOPE_club:write" },
                                    GrantedAuthority { "SCOPE_project:read" },
                                )
                        }

                    it("true를 반환해야 한다") {
                        scopeChecker.hasAllScopes(mockAuth, setOf("student:read", "club:write")) shouldBe true
                    }
                }

                context("와일드카드로 모든 scope를 커버할 때") {
                    val mockAuth =
                        mockk<Authentication> {
                            every { authorities } returns
                                listOf(
                                    GrantedAuthority { "SCOPE_student:*" },
                                    GrantedAuthority { "SCOPE_club:*" },
                                )
                        }

                    it("true를 반환해야 한다") {
                        scopeChecker.hasAllScopes(
                            mockAuth,
                            setOf("student:read", "student:write", "club:read"),
                        ) shouldBe true
                    }
                }

                context("일부 scope가 없을 때") {
                    val mockAuth =
                        mockk<Authentication> {
                            every { authorities } returns
                                listOf(
                                    GrantedAuthority { "SCOPE_student:read" },
                                    GrantedAuthority { "SCOPE_club:write" },
                                )
                        }

                    it("false를 반환해야 한다") {
                        scopeChecker.hasAllScopes(mockAuth, setOf("student:read", "project:read")) shouldBe false
                    }
                }

                context("빈 요청 scope 집합일 때") {
                    val mockAuth =
                        mockk<Authentication> {
                            every { authorities } returns
                                listOf(
                                    GrantedAuthority { "SCOPE_student:read" },
                                )
                        }

                    it("true를 반환해야 한다") {
                        scopeChecker.hasAllScopes(mockAuth, emptySet()) shouldBe true
                    }
                }
            }
        }
    })
