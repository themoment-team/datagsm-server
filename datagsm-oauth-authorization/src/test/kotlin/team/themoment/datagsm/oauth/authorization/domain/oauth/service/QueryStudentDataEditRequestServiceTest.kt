package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
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
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl.QueryStudentDataEditRequestServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class QueryStudentDataEditRequestServiceTest :
    DescribeSpec({

        val mockAccountJpaRepository = mockk<AccountJpaRepository>()
        val mockStudentDataEditRequestJpaRepository = mockk<StudentDataEditRequestJpaRepository>()
        val mockClubJpaRepository = mockk<ClubJpaRepository>()
        val mockPasswordEncoder = mockk<PasswordEncoder>()

        val service =
            QueryStudentDataEditRequestServiceImpl(
                mockAccountJpaRepository,
                mockStudentDataEditRequestJpaRepository,
                mockClubJpaRepository,
                mockPasswordEncoder,
            )

        afterEach {
            clearAllMocks()
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

                    it("운영 중인 동아리만 옵션으로 반환되어야 한다") {
                        val result = service.execute(reqDto)

                        result.fields.size shouldBe 1
                        val majorClubField = result.fields.first()
                        majorClubField.name shouldBe StudentDataEditField.MAJOR_CLUB
                        majorClubField.options shouldContainExactly listOf(DataEditOptionResDto(value = 1L, label = "SW개발동아리"))
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
