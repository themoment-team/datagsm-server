package team.themoment.datagsm.web.domain.student.service.impl

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.oauth.entity.IdpSessionRedisEntity
import team.themoment.datagsm.common.domain.oauth.entity.OauthRefreshTokenRedisEntity
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthRefreshTokenRedisRepository
import team.themoment.datagsm.common.domain.student.dto.request.RequestStudentDataEditReqDto
import team.themoment.datagsm.common.domain.student.entity.StudentDataEditRequestJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentDataEditField
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentDataEditRequestJpaRepository
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository

class RequestStudentDataEditServiceImplTest :
    DescribeSpec({

        lateinit var mockStudentJpaRepository: StudentJpaRepository
        lateinit var mockStudentDataEditRequestJpaRepository: StudentDataEditRequestJpaRepository
        lateinit var mockAccountJpaRepository: AccountJpaRepository
        lateinit var mockIdpSessionRedisRepository: IdpSessionRedisRepository
        lateinit var mockOauthRefreshTokenRedisRepository: OauthRefreshTokenRedisRepository
        lateinit var mockApplicationEventPublisher: ApplicationEventPublisher
        lateinit var service: RequestStudentDataEditServiceImpl

        val studentId = 210L
        val email = "s25012@gsm.hs.kr"

        fun student() =
            StudentJpaEntity().apply {
                id = studentId
                name = "홍길동"
                this.email = email
                sex = Sex.MAN
                role = StudentRole.GENERAL_STUDENT
            }

        fun account() =
            AccountJpaEntity().apply {
                id = 13L
                this.email = email
                objectType = AccountObjectType.STUDENT
                objectId = studentId
            }

        val idpSession = IdpSessionRedisEntity(sessionId = "session-1", email = email, ttl = 28800L)
        val refreshToken =
            OauthRefreshTokenRedisEntity.of(
                email = email,
                clientId = "datagsm-web",
                token = "refresh-token",
                scopes = setOf("self:read"),
                ttl = 2592000L,
            )

        beforeEach {
            mockStudentJpaRepository = mockk()
            mockStudentDataEditRequestJpaRepository = mockk()
            mockAccountJpaRepository = mockk()
            mockIdpSessionRedisRepository = mockk()
            mockOauthRefreshTokenRedisRepository = mockk()
            mockApplicationEventPublisher = mockk()
            service =
                RequestStudentDataEditServiceImpl(
                    mockStudentJpaRepository,
                    mockStudentDataEditRequestJpaRepository,
                    mockAccountJpaRepository,
                    mockIdpSessionRedisRepository,
                    mockOauthRefreshTokenRedisRepository,
                    mockApplicationEventPublisher,
                )

            every { mockStudentDataEditRequestJpaRepository.findAllByStudentIdIn(any()) } returns emptyList()
            every { mockStudentDataEditRequestJpaRepository.save(any()) } answers { firstArg() }
            justRun { mockApplicationEventPublisher.publishEvent(any<EventDispatchRequested>()) }
            justRun { mockIdpSessionRedisRepository.deleteAll(any<Iterable<IdpSessionRedisEntity>>()) }
            justRun { mockOauthRefreshTokenRedisRepository.deleteAll(any<Iterable<OauthRefreshTokenRedisEntity>>()) }
        }

        describe("RequestStudentDataEditServiceImpl 클래스의") {
            describe("execute 메서드는") {
                val reqDto =
                    RequestStudentDataEditReqDto(
                        studentIds = listOf(studentId),
                        fields = setOf(StudentDataEditField.MAJOR_CLUB),
                    )

                context("대상 학생에게 연결된 계정이 로그인 중일 때") {
                    beforeEach {
                        every { mockStudentJpaRepository.findAllById(listOf(studentId)) } returns listOf(student())
                        every {
                            mockAccountJpaRepository.findAllByObjectTypeAndObjectIdIn(AccountObjectType.STUDENT, listOf(studentId))
                        } returns listOf(account())
                        every { mockIdpSessionRedisRepository.findAllByEmail(email) } returns listOf(idpSession)
                        every { mockOauthRefreshTokenRedisRepository.findAllByEmail(email) } returns listOf(refreshToken)
                    }

                    it("정보 수정 요청을 저장하고 해당 계정의 IdP 세션과 refresh token을 모두 폐기한다") {
                        service.execute(reqDto)

                        verify(exactly = 1) {
                            mockStudentDataEditRequestJpaRepository.save(match { it.studentId == studentId && it.requestMajorClub })
                        }
                        verify(exactly = 1) { mockIdpSessionRedisRepository.deleteAll(listOf(idpSession)) }
                        verify(exactly = 1) { mockOauthRefreshTokenRedisRepository.deleteAll(listOf(refreshToken)) }
                    }

                    it("요청한 필드를 비운다") {
                        val target = student()
                        every { mockStudentJpaRepository.findAllById(listOf(studentId)) } returns listOf(target)

                        service.execute(reqDto)

                        target.majorClub shouldBe null
                    }
                }

                context("대상 학생에게 연결된 계정이 없을 때") {
                    beforeEach {
                        every { mockStudentJpaRepository.findAllById(listOf(studentId)) } returns listOf(student())
                        every {
                            mockAccountJpaRepository.findAllByObjectTypeAndObjectIdIn(AccountObjectType.STUDENT, listOf(studentId))
                        } returns emptyList()
                    }

                    it("폐기할 세션이 없으므로 Redis를 조회하지 않고 요청만 저장한다") {
                        service.execute(reqDto)

                        verify(exactly = 1) { mockStudentDataEditRequestJpaRepository.save(any<StudentDataEditRequestJpaEntity>()) }
                        verify(exactly = 0) { mockIdpSessionRedisRepository.findAllByEmail(any()) }
                        verify(exactly = 0) { mockOauthRefreshTokenRedisRepository.findAllByEmail(any()) }
                    }
                }
            }
        }
    })
