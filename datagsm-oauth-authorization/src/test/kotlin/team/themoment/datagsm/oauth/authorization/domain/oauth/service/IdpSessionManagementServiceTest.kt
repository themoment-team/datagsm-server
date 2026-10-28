package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.oauth.entity.IdpSessionRedisEntity
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.common.global.security.util.OpaqueTokenHashUtil
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.DeleteIdpSessionServiceImpl
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.QueryIdpSessionServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class IdpSessionManagementServiceTest :
    DescribeSpec({

        val mockIdpSessionRedisRepository = mockk<IdpSessionRedisRepository>(relaxed = true)
        val mockOauthEnvironment = mockk<OauthEnvironment>()

        val queryIdpSessionService = QueryIdpSessionServiceImpl(mockIdpSessionRedisRepository)
        val deleteIdpSessionService =
            DeleteIdpSessionServiceImpl(mockIdpSessionRedisRepository, mockOauthEnvironment)

        afterEach {
            clearAllMocks()
        }

        val myEmail = "user@gsm.hs.kr"
        val otherEmail = "other@gsm.hs.kr"
        val currentSessionId = "session-current"
        val otherDeviceSessionId = "session-other-device"
        val foreignSessionId = "session-foreign"

        fun session(
            id: String,
            email: String,
            userAgent: String? = null,
            createdAt: Long? = null,
        ) = IdpSessionRedisEntity(
            sessionId = id,
            email = email,
            userAgent = userAgent,
            createdAt = createdAt,
            ttl = 28800,
        )

        fun handleOf(sessionId: String) = OpaqueTokenHashUtil.hash(sessionId)

        describe("QueryIdpSessionService 클래스의") {
            describe("execute 메서드는") {

                beforeEach {
                    every { mockIdpSessionRedisRepository.findById(currentSessionId) } returns
                        Optional.of(session(currentSessionId, myEmail, "Chrome/120", 2000L))
                    every { mockIdpSessionRedisRepository.findAllByEmail(myEmail) } returns
                        listOf(
                            session(currentSessionId, myEmail, "Chrome/120", 2000L),
                            session(otherDeviceSessionId, myEmail, "Safari/17", 3000L),
                        )
                }

                context("유효한 세션 쿠키가 주어졌을 때") {
                    it("해당 계정의 세션 목록을 반환해야 한다") {
                        val result = queryIdpSessionService.execute(currentSessionId)

                        result.sessions.map { it.sessionId }.toSet() shouldBe
                            setOf(handleOf(currentSessionId), handleOf(otherDeviceSessionId))
                    }

                    // 세션 ID는 곧 쿠키 값이라, 응답에 실리면 HttpOnly로 숨긴 의미가 사라진다.
                    it("쿠키 값인 원본 세션 ID를 내려주지 않아야 한다") {
                        val result = queryIdpSessionService.execute(currentSessionId)

                        result.sessions.map { it.sessionId } shouldNotContainAnyOf
                            listOf(currentSessionId, otherDeviceSessionId)
                    }

                    it("최근 로그인 순으로 정렬되어야 한다") {
                        val result = queryIdpSessionService.execute(currentSessionId)

                        result.sessions.map { it.sessionId } shouldBe
                            listOf(handleOf(otherDeviceSessionId), handleOf(currentSessionId))
                    }

                    it("현재 사용 중인 세션을 표시해야 한다") {
                        val result = queryIdpSessionService.execute(currentSessionId)

                        result.sessions.single { it.current }.sessionId shouldBe handleOf(currentSessionId)
                    }

                    it("기기를 구분할 수 있도록 User-Agent를 함께 내려줘야 한다") {
                        val result = queryIdpSessionService.execute(currentSessionId)

                        result.sessions.single { it.sessionId == handleOf(otherDeviceSessionId) }.userAgent shouldBe "Safari/17"
                    }
                }

                context("세션 쿠키가 없을 때") {
                    it("401이 반환되어야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> { queryIdpSessionService.execute(null) }

                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED
                    }
                }

                context("세션이 만료되었을 때") {
                    it("401이 반환되어야 한다") {
                        every { mockIdpSessionRedisRepository.findById(currentSessionId) } returns Optional.empty()

                        shouldThrow<ExpectedException> { queryIdpSessionService.execute(currentSessionId) }
                    }
                }
            }
        }

        describe("DeleteIdpSessionService 클래스의") {
            describe("execute 메서드는") {

                beforeEach {
                    every { mockOauthEnvironment.idpSessionCookieName } returns "datagsm_idp_session"
                    every { mockOauthEnvironment.idpSessionCookieSecure } returns true
                    every { mockIdpSessionRedisRepository.findById(currentSessionId) } returns
                        Optional.of(session(currentSessionId, myEmail))
                    every { mockIdpSessionRedisRepository.findAllByEmail(myEmail) } returns
                        listOf(session(currentSessionId, myEmail), session(otherDeviceSessionId, myEmail))
                    every { mockIdpSessionRedisRepository.findAllByEmail(otherEmail) } returns
                        listOf(session(foreignSessionId, otherEmail))
                    // 구현이 식별자로 직접 조회하는 방식으로 되돌아가면 아래 테스트가 실패하도록, 직접 조회 경로에도 세션을 걸어 둔다.
                    every { mockIdpSessionRedisRepository.findById(handleOf(foreignSessionId)) } returns
                        Optional.of(session(foreignSessionId, otherEmail))
                    every { mockIdpSessionRedisRepository.findById(otherDeviceSessionId) } returns
                        Optional.of(session(otherDeviceSessionId, myEmail))
                }

                context("세션을 조회할 때") {
                    it("본인 계정의 세션 목록으로만 조회해야 한다") {
                        deleteIdpSessionService.execute(handleOf(otherDeviceSessionId), currentSessionId)

                        verify(exactly = 1) { mockIdpSessionRedisRepository.findAllByEmail(myEmail) }
                        verify(exactly = 0) { mockIdpSessionRedisRepository.findAllByEmail(otherEmail) }
                    }
                }

                // 이메일 인덱스에 다른 계정의 세션이 잘못 남아 있어도, 소유권은 코드에서 다시 확인한다.
                context("본인 이메일 인덱스에 다른 계정의 세션이 섞여 있을 때") {
                    it("그 세션은 삭제하지 않고 404가 반환되어야 한다") {
                        every { mockIdpSessionRedisRepository.findAllByEmail(myEmail) } returns
                            listOf(session(currentSessionId, myEmail), session(foreignSessionId, otherEmail))

                        val exception =
                            shouldThrow<ExpectedException> {
                                deleteIdpSessionService.execute(handleOf(foreignSessionId), currentSessionId)
                            }

                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                        verify(exactly = 0) { mockIdpSessionRedisRepository.deleteById(any()) }
                    }
                }

                context("본인의 다른 기기 세션을 목록의 식별자로 종료할 때") {
                    it("해당 세션이 삭제되어야 한다") {
                        val response =
                            deleteIdpSessionService.execute(handleOf(otherDeviceSessionId), currentSessionId)

                        response.statusCode shouldBe HttpStatus.NO_CONTENT
                        verify(exactly = 1) { mockIdpSessionRedisRepository.deleteById(otherDeviceSessionId) }
                    }

                    it("현재 쿠키는 건드리지 않아야 한다") {
                        val response =
                            deleteIdpSessionService.execute(handleOf(otherDeviceSessionId), currentSessionId)

                        response.headers.getFirst(HttpHeaders.SET_COOKIE) shouldBe null
                    }
                }

                context("현재 사용 중인 세션을 종료할 때") {
                    it("죽은 세션이 남지 않도록 쿠키도 만료시켜야 한다") {
                        val response =
                            deleteIdpSessionService.execute(handleOf(currentSessionId), currentSessionId)

                        verify(exactly = 1) { mockIdpSessionRedisRepository.deleteById(currentSessionId) }
                        (response.headers.getFirst(HttpHeaders.SET_COOKIE) ?: "") shouldContain "Max-Age=0"
                    }
                }

                // 세션 식별자를 알아낸 것만으로 남의 세션을 끊을 수 있으면 안 된다.
                context("다른 계정의 세션을 종료하려 할 때") {
                    it("삭제하지 않고 404가 반환되어야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                deleteIdpSessionService.execute(handleOf(foreignSessionId), currentSessionId)
                            }

                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                        verify(exactly = 0) { mockIdpSessionRedisRepository.deleteById(any()) }
                    }

                    // 존재하지 않는 세션과 응답이 달라지면 세션 존재 여부를 탐지할 수 있게 된다.
                    it("존재하지 않는 세션과 같은 상태 코드여야 한다") {
                        val foreign =
                            shouldThrow<ExpectedException> {
                                deleteIdpSessionService.execute(handleOf(foreignSessionId), currentSessionId)
                            }
                        val unknown =
                            shouldThrow<ExpectedException> {
                                deleteIdpSessionService.execute(handleOf("unknown"), currentSessionId)
                            }

                        foreign.statusCode shouldBe unknown.statusCode
                        foreign.message shouldBe unknown.message
                    }
                }

                // 목록에는 해시만 내려가므로, 원본 ID를 받는 경로가 남아 있으면 안 된다.
                context("원본 세션 ID로 종료하려 할 때") {
                    it("삭제하지 않고 404가 반환되어야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                deleteIdpSessionService.execute(otherDeviceSessionId, currentSessionId)
                            }

                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                        verify(exactly = 0) { mockIdpSessionRedisRepository.deleteById(any()) }
                    }
                }

                context("세션 쿠키가 없을 때") {
                    it("401이 반환되고 삭제가 일어나지 않아야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                deleteIdpSessionService.execute(handleOf(otherDeviceSessionId), null)
                            }

                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED
                        verify(exactly = 0) { mockIdpSessionRedisRepository.deleteById(any()) }
                    }
                }
            }
        }
    })
