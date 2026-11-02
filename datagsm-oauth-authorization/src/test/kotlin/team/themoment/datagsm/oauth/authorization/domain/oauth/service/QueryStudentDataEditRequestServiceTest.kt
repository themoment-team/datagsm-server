package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.dto.request.QueryStudentDataEditRequestReqDto
import team.themoment.datagsm.common.domain.student.dto.response.DataEditOptionResDto
import team.themoment.datagsm.common.domain.student.entity.StudentDataEditRequestJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.StudentDataEditField
import team.themoment.datagsm.common.domain.student.repository.StudentDataEditRequestJpaRepository
import team.themoment.datagsm.common.global.dto.internal.RateLimitConsumeResult
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.QueryStudentDataEditRequestServiceImpl
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimitType
import team.themoment.datagsm.oauth.authorization.global.security.service.PasswordResetRateLimitService
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class QueryStudentDataEditRequestServiceTest :
    DescribeSpec({

        val mockAccountJpaRepository = mockk<AccountJpaRepository>()
        val mockStudentDataEditRequestJpaRepository = mockk<StudentDataEditRequestJpaRepository>()
        val mockClubJpaRepository = mockk<ClubJpaRepository>()
        val mockPasswordEncoder = mockk<PasswordEncoder>()
        val mockPasswordResetRateLimitService = mockk<PasswordResetRateLimitService>(relaxUnitFun = true)

        val service =
            QueryStudentDataEditRequestServiceImpl(
                mockAccountJpaRepository,
                mockStudentDataEditRequestJpaRepository,
                mockClubJpaRepository,
                mockPasswordEncoder,
                mockPasswordResetRateLimitService,
            )

        afterEach {
            clearAllMocks()
        }

        beforeEach {
            every { mockPasswordResetRateLimitService.tryConsume(any(), any()) } returns
                RateLimitConsumeResult(consumed = true, remainingTokens = 9, secondsToWaitForRefill = 0)
        }

        describe("QueryStudentDataEditRequestService 클래스의") {
            describe("execute 메서드는") {

                val testEmail = "user@gsm.hs.kr"
                val testPassword = "password123!"

                val studentAccount =
                    AccountJpaEntity().apply {
                        id = 1L
                        email = testEmail
                        password = "hashedPassword"
                        objectId = 10L
                        objectType = AccountObjectType.STUDENT
                    }

                context("이메일 또는 비밀번호가 일치하지 않을 때") {
                    val reqDto = QueryStudentDataEditRequestReqDto(email = testEmail, password = testPassword)

                    beforeEach {
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(studentAccount)
                        every { mockPasswordEncoder.matches(testPassword, studentAccount.password) } returns false
                    }

                    it("ExpectedException 예외가 발생해야 한다") {
                        val exception = shouldThrow<ExpectedException> { service.execute(reqDto) }

                        exception.message shouldBe "이메일 또는 비밀번호가 일치하지 않습니다."
                        exception.statusCode shouldBe HttpStatus.UNAUTHORIZED
                    }

                    // 로그인과 같은 비밀번호 검증 입구라, 실패 횟수를 로그인 한도와 함께 센다.
                    it("로그인 한도를 차감하고 되돌리지 않아야 한다") {
                        shouldThrow<ExpectedException> { service.execute(reqDto) }

                        verify(exactly = 1) { mockPasswordResetRateLimitService.tryConsume(testEmail, PasswordResetRateLimitType.LOGIN) }
                        verify(exactly = 0) { mockPasswordResetRateLimitService.refund(any(), any()) }
                    }
                }

                context("로그인 시도 한도를 소진한 계정으로 요청할 때") {
                    val reqDto = QueryStudentDataEditRequestReqDto(email = testEmail, password = testPassword)

                    beforeEach {
                        every { mockPasswordResetRateLimitService.tryConsume(any(), any()) } returns
                            RateLimitConsumeResult(consumed = false, remainingTokens = 0, secondsToWaitForRefill = 600)
                    }

                    it("비밀번호 검증 없이 429 예외가 발생해야 한다") {
                        val exception = shouldThrow<ExpectedException> { service.execute(reqDto) }

                        exception.statusCode shouldBe HttpStatus.TOO_MANY_REQUESTS
                        verify(exactly = 0) { mockAccountJpaRepository.findByEmail(any()) }
                    }
                }

                context("학생 정보가 연결되지 않은 계정일 때") {
                    val reqDto = QueryStudentDataEditRequestReqDto(email = testEmail, password = testPassword)
                    val teacherAccount =
                        AccountJpaEntity().apply {
                            id = 2L
                            email = testEmail
                            password = "hashedPassword"
                            objectType = null
                        }

                    beforeEach {
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(teacherAccount)
                        every { mockPasswordEncoder.matches(testPassword, teacherAccount.password) } returns true
                    }

                    it("FORBIDDEN ExpectedException 예외가 발생해야 한다") {
                        val exception = shouldThrow<ExpectedException> { service.execute(reqDto) }

                        exception.message shouldBe "학생 정보가 연결되지 않은 계정입니다."
                        exception.statusCode shouldBe HttpStatus.FORBIDDEN
                    }
                }

                context("진행 중인 정보 수정 요청이 없을 때") {
                    val reqDto = QueryStudentDataEditRequestReqDto(email = testEmail, password = testPassword)

                    beforeEach {
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(studentAccount)
                        every { mockPasswordEncoder.matches(testPassword, studentAccount.password) } returns true
                        every { mockStudentDataEditRequestJpaRepository.findByStudentId(10L) } returns Optional.empty()
                    }

                    it("NOT_FOUND ExpectedException 예외가 발생해야 한다") {
                        val exception = shouldThrow<ExpectedException> { service.execute(reqDto) }

                        exception.message shouldBe "진행 중인 정보 수정 요청이 없습니다."
                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                    }

                    it("비밀번호가 맞았으므로 차감한 로그인 시도를 되돌려야 한다") {
                        shouldThrow<ExpectedException> { service.execute(reqDto) }

                        verify(exactly = 1) { mockPasswordResetRateLimitService.refund(testEmail, PasswordResetRateLimitType.LOGIN) }
                    }
                }

                context("전공동아리 수정이 요청되어 있을 때") {
                    val reqDto = QueryStudentDataEditRequestReqDto(email = testEmail, password = testPassword)
                    val editRequest =
                        StudentDataEditRequestJpaEntity().apply {
                            studentId = 10L
                            requestMajorClub = true
                        }
                    val activeClub =
                        ClubJpaEntity().apply {
                            id = 1L
                            name = "SW개발동아리"
                            type = ClubType.MAJOR_CLUB
                            status = ClubStatus.ACTIVE
                        }
                    val abolishedClub =
                        ClubJpaEntity().apply {
                            id = 2L
                            name = "폐지된동아리"
                            type = ClubType.MAJOR_CLUB
                            status = ClubStatus.ABOLISHED
                        }

                    beforeEach {
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(studentAccount)
                        every { mockPasswordEncoder.matches(testPassword, studentAccount.password) } returns true
                        every { mockStudentDataEditRequestJpaRepository.findByStudentId(10L) } returns Optional.of(editRequest)
                        every { mockClubJpaRepository.findByType(ClubType.MAJOR_CLUB) } returns listOf(activeClub, abolishedClub)
                    }

                    it("무소속 옵션과 운영 중인 동아리만 옵션으로 반환되어야 한다") {
                        val result = service.execute(reqDto)

                        result.fields.size shouldBe 1
                        val majorClubField = result.fields.first()
                        majorClubField.name shouldBe StudentDataEditField.MAJOR_CLUB
                        majorClubField.options shouldContainExactly
                            listOf(
                                DataEditOptionResDto(value = 0L, label = "무소속"),
                                DataEditOptionResDto(value = 1L, label = "SW개발동아리"),
                            )
                    }
                }

                context("학번 수정만 요청되어 있을 때") {
                    val reqDto = QueryStudentDataEditRequestReqDto(email = testEmail, password = testPassword)
                    val editRequest =
                        StudentDataEditRequestJpaEntity().apply {
                            studentId = 10L
                            requestStudentNumber = true
                        }

                    beforeEach {
                        every { mockAccountJpaRepository.findByEmail(testEmail) } returns Optional.of(studentAccount)
                        every { mockPasswordEncoder.matches(testPassword, studentAccount.password) } returns true
                        every { mockStudentDataEditRequestJpaRepository.findByStudentId(10L) } returns Optional.of(editRequest)
                    }

                    it("동아리 조회 없이 옵션이 없는 필드만 반환되어야 한다") {
                        val result = service.execute(reqDto)

                        result.fields.size shouldBe 1
                        result.fields.first().name shouldBe StudentDataEditField.STUDENT_NUMBER
                        result.fields.first().options shouldBe null
                    }
                }
            }
        }
    })
