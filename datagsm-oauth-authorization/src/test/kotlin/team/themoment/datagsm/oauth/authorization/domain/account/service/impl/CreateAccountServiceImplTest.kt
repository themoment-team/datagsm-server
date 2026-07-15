package team.themoment.datagsm.oauth.authorization.domain.account.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import team.themoment.datagsm.common.domain.account.dto.request.CreateAccountReqDto
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.EmailCodeRedisEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.common.domain.teacher.entity.TeacherJpaEntity
import team.themoment.datagsm.common.domain.teacher.entity.constant.TeacherDepartment
import team.themoment.datagsm.common.domain.teacher.repository.TeacherJpaRepository
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class CreateAccountServiceImplTest :
    BehaviorSpec({
        val accountJpaRepository = mockk<AccountJpaRepository>()
        val studentJpaRepository = mockk<StudentJpaRepository>()
        val teacherJpaRepository = mockk<TeacherJpaRepository>()
        val emailCodeRedisRepository = mockk<EmailCodeRedisRepository>(relaxed = true)
        val passwordEncoder = mockk<PasswordEncoder>()

        val service =
            CreateAccountServiceImpl(
                accountJpaRepository,
                studentJpaRepository,
                teacherJpaRepository,
                emailCodeRedisRepository,
                passwordEncoder,
            )

        Given("이미 존재하는 이메일로") {
            val email = "existing@gsm.hs.kr"
            val reqDto = CreateAccountReqDto(email = email, password = "password123", code = "12345678")
            val existingAccount = mockk<AccountJpaEntity>()

            every { accountJpaRepository.findByEmail(email) } returns Optional.of(existingAccount)

            When("계정 생성을 요청하면") {
                Then("409 Conflict 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(reqDto)
                        }

                    exception.message shouldBe "이미 해당 이메일을 가진 계정이 존재합니다."
                }
            }
        }

        Given("학생 가입에서 Student가 없을 때") {
            val email = "new@gsm.hs.kr"
            val password = "password123"
            val code = "12345678"
            val encodedPassword = "encodedPassword"
            val reqDto = CreateAccountReqDto(email = email, password = password, code = code)
            val emailCodeEntity = EmailCodeRedisEntity(email = email, code = code, ttl = 300)
            val accountSlot = slot<AccountJpaEntity>()
            val savedAccount = mockk<AccountJpaEntity>()

            every { accountJpaRepository.findByEmail(email) } returns Optional.empty()
            every { emailCodeRedisRepository.findByIdOrNull(email) } returns emailCodeEntity
            every { studentJpaRepository.findByEmail(email) } returns Optional.empty()
            every { passwordEncoder.encode(password) } returns encodedPassword
            every { accountJpaRepository.save(capture(accountSlot)) } returns savedAccount

            When("계정 생성을 요청하면") {
                service.execute(reqDto)

                Then("인증 코드가 검증되고 objectId 없이 ACTIVE 계정이 생성된다") {
                    verify(exactly = 1) { emailCodeRedisRepository.findByIdOrNull(email) }
                    verify(exactly = 1) { emailCodeRedisRepository.deleteById(email) }
                    verify(exactly = 1) { accountJpaRepository.save(any()) }

                    val capturedAccount = accountSlot.captured
                    capturedAccount.email shouldBe email
                    capturedAccount.password shouldBe encodedPassword
                    capturedAccount.role shouldBe AccountRole.USER
                    capturedAccount.objectType shouldBe AccountObjectType.STUDENT
                    capturedAccount.status shouldBe AccountStatus.ACTIVE
                    capturedAccount.objectId.shouldBeNull()
                }
            }
        }

        Given("학생 가입에서 Student가 있을 때") {
            val email = "student@gsm.hs.kr"
            val password = "password123"
            val code = "12345678"
            val encodedPassword = "encodedPassword"
            val reqDto = CreateAccountReqDto(email = email, password = password, code = code)
            val emailCodeEntity = EmailCodeRedisEntity(email = email, code = code, ttl = 300)
            val student = StudentJpaEntity().apply { id = 42L }
            val accountSlot = slot<AccountJpaEntity>()
            val savedAccount = mockk<AccountJpaEntity>()

            every { accountJpaRepository.findByEmail(email) } returns Optional.empty()
            every { emailCodeRedisRepository.findByIdOrNull(email) } returns emailCodeEntity
            every { studentJpaRepository.findByEmail(email) } returns Optional.of(student)
            every { passwordEncoder.encode(password) } returns encodedPassword
            every { accountJpaRepository.save(capture(accountSlot)) } returns savedAccount

            When("계정 생성을 요청하면") {
                service.execute(reqDto)

                Then("Student가 objectId로 연결된다") {
                    val capturedAccount = accountSlot.captured
                    capturedAccount.objectType shouldBe AccountObjectType.STUDENT
                    capturedAccount.status shouldBe AccountStatus.ACTIVE
                    capturedAccount.objectId shouldBe 42L
                }
            }
        }

        Given("선생님 가입 요청일 때") {
            val email = "teacher@gsm.hs.kr"
            val password = "password123"
            val code = "12345678"
            val encodedPassword = "encodedPassword"
            val reqDto =
                CreateAccountReqDto(
                    email = email,
                    password = password,
                    code = code,
                    objectType = AccountObjectType.TEACHER,
                    name = "김선생",
                    department = TeacherDepartment.GRADE,
                    description = "3학년 1반 담임선생님",
                )
            val emailCodeEntity = EmailCodeRedisEntity(email = email, code = code, ttl = 300)
            val savedTeacher =
                TeacherJpaEntity
                    .create("김선생", email, TeacherDepartment.GRADE, "3학년 1반 담임선생님")
                    .apply { id = 7L }
            val accountSlot = slot<AccountJpaEntity>()
            val savedAccount = mockk<AccountJpaEntity>()

            every { accountJpaRepository.findByEmail(email) } returns Optional.empty()
            every { emailCodeRedisRepository.findByIdOrNull(email) } returns emailCodeEntity
            every { teacherJpaRepository.findByEmail(email) } returns Optional.empty()
            every { teacherJpaRepository.save(any()) } returns savedTeacher
            every { passwordEncoder.encode(password) } returns encodedPassword
            every { accountJpaRepository.save(capture(accountSlot)) } returns savedAccount

            When("계정 생성을 요청하면") {
                service.execute(reqDto)

                Then("Teacher가 생성되고 PENDING 계정이 만들어진다") {
                    verify(exactly = 1) { teacherJpaRepository.save(any()) }

                    val capturedAccount = accountSlot.captured
                    capturedAccount.objectType shouldBe AccountObjectType.TEACHER
                    capturedAccount.status shouldBe AccountStatus.PENDING
                    capturedAccount.objectId shouldBe 7L
                }
            }
        }

        Given("선생님 가입에서 이름이 없을 때") {
            val email = "noname@gsm.hs.kr"
            val code = "12345678"
            val reqDto =
                CreateAccountReqDto(
                    email = email,
                    password = "password123",
                    code = code,
                    objectType = AccountObjectType.TEACHER,
                    name = null,
                )
            val emailCodeEntity = EmailCodeRedisEntity(email = email, code = code, ttl = 300)

            every { accountJpaRepository.findByEmail(email) } returns Optional.empty()
            every { emailCodeRedisRepository.findByIdOrNull(email) } returns emailCodeEntity

            When("계정 생성을 요청하면") {
                Then("400 Bad Request 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(reqDto)
                        }

                    exception.message shouldBe "선생님 가입 시 이름은 필수입니다."
                }
            }
        }

        Given("선생님 가입에서 소속 부서가 없을 때") {
            val email = "nodept@gsm.hs.kr"
            val code = "12345678"
            val reqDto =
                CreateAccountReqDto(
                    email = email,
                    password = "password123",
                    code = code,
                    objectType = AccountObjectType.TEACHER,
                    name = "김선생",
                    department = null,
                )
            val emailCodeEntity = EmailCodeRedisEntity(email = email, code = code, ttl = 300)

            every { accountJpaRepository.findByEmail(email) } returns Optional.empty()
            every { emailCodeRedisRepository.findByIdOrNull(email) } returns emailCodeEntity

            When("계정 생성을 요청하면") {
                Then("400 Bad Request 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(reqDto)
                        }

                    exception.message shouldBe "선생님 가입 시 소속 부서는 필수입니다."
                }
            }
        }

        Given("선생님 가입에서 이미 존재하는 선생님 이메일일 때") {
            val email = "dupteacher@gsm.hs.kr"
            val code = "12345678"
            val reqDto =
                CreateAccountReqDto(
                    email = email,
                    password = "password123",
                    code = code,
                    objectType = AccountObjectType.TEACHER,
                    name = "김선생",
                    department = TeacherDepartment.GRADE,
                )
            val emailCodeEntity = EmailCodeRedisEntity(email = email, code = code, ttl = 300)
            val existingTeacher = TeacherJpaEntity.create("이선생", email, TeacherDepartment.GRADE, null)

            every { accountJpaRepository.findByEmail(email) } returns Optional.empty()
            every { emailCodeRedisRepository.findByIdOrNull(email) } returns emailCodeEntity
            every { teacherJpaRepository.findByEmail(email) } returns Optional.of(existingTeacher)

            When("계정 생성을 요청하면") {
                Then("409 Conflict 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(reqDto)
                        }

                    exception.message shouldBe "이미 해당 이메일을 가진 선생님이 존재합니다."
                }
            }
        }
    })
