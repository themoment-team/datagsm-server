package team.themoment.datagsm.web.domain.account.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.student.entity.DormitoryRoomNumber
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.web.domain.account.service.impl.QueryMyInfoServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider

class QueryMyInfoServiceTest :
    DescribeSpec({

        lateinit var mockCurrentUserProvider: CurrentUserProvider
        lateinit var queryMyInfoService: QueryMyInfoService

        beforeEach {
            mockCurrentUserProvider = mockk<CurrentUserProvider>()
            queryMyInfoService = QueryMyInfoServiceImpl(mockCurrentUserProvider)
        }

        describe("QueryMyInfoService 클래스의") {
            describe("execute 메서드는") {

                context("학생 정보가 없는 계정일 때") {
                    lateinit var account: AccountJpaEntity

                    beforeEach {
                        account =
                            AccountJpaEntity().apply {
                                id = 1L
                                email = "admin@gsm.hs.kr"
                                password = "encoded_password"
                                role = AccountRole.ADMIN
                                student = null
                            }
                        every { mockCurrentUserProvider.getCurrentAccount() } returns account
                    }

                    it("학생 정보 없이 계정 정보를 반환해야 한다") {
                        val result = queryMyInfoService.execute()

                        result.id shouldBe 1L
                        result.email shouldBe "admin@gsm.hs.kr"
                        result.role shouldBe AccountRole.ADMIN
                        result.isStudent shouldBe false
                        result.student shouldBe null

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }

                context("학생 정보가 있는 계정일 때") {
                    lateinit var account: AccountJpaEntity
                    lateinit var student: StudentJpaEntity

                    beforeEach {
                        student =
                            StudentJpaEntity().apply {
                                id = 10L
                                name = "홍길동"
                                sex = Sex.MAN
                                email = "hong@gsm.hs.kr"
                                studentNumber = StudentNumber(1, 2, 3)
                                major = Major.SW_DEVELOPMENT
                                dormitoryRoomNumber = DormitoryRoomNumber(201)
                            }
                        account =
                            AccountJpaEntity().apply {
                                id = 2L
                                email = "hong@gsm.hs.kr"
                                password = "encoded_password"
                                role = AccountRole.USER
                                this.student = student
                            }
                        every { mockCurrentUserProvider.getCurrentAccount() } returns account
                    }

                    it("학생 정보를 포함하여 계정 정보를 반환해야 한다") {
                        val result = queryMyInfoService.execute()

                        result.id shouldBe 2L
                        result.email shouldBe "hong@gsm.hs.kr"
                        result.role shouldBe AccountRole.USER
                        result.isStudent shouldBe true

                        val studentDto = result.student!!
                        studentDto.id shouldBe 10L
                        studentDto.name shouldBe "홍길동"
                        studentDto.sex shouldBe Sex.MAN
                        studentDto.email shouldBe "hong@gsm.hs.kr"
                        studentDto.grade shouldBe 1
                        studentDto.classNum shouldBe 2
                        studentDto.number shouldBe 3
                        studentDto.studentNumber shouldBe 1203
                        studentDto.major shouldBe Major.SW_DEVELOPMENT
                        studentDto.dormitoryFloor shouldBe 2
                        studentDto.dormitoryRoom shouldBe 201

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentAccount() }
                    }
                }

                context("동아리 정보가 있는 학생 계정일 때") {
                    lateinit var account: AccountJpaEntity
                    lateinit var student: StudentJpaEntity

                    beforeEach {
                        val majorClub =
                            ClubJpaEntity().apply {
                                id = 100L
                                name = "SW개발동아리"
                                type = ClubType.MAJOR_CLUB
                            }
                        val autonomousClub =
                            ClubJpaEntity().apply {
                                id = 300L
                                name = "자율동아리"
                                type = ClubType.AUTONOMOUS_CLUB
                            }
                        student =
                            StudentJpaEntity().apply {
                                id = 20L
                                name = "김철수"
                                sex = Sex.MAN
                                email = "kim@gsm.hs.kr"
                                studentNumber = StudentNumber(2, 1, 5)
                                major = Major.AI
                                this.majorClub = majorClub
                                this.autonomousClub = autonomousClub
                            }
                        account =
                            AccountJpaEntity().apply {
                                id = 3L
                                email = "kim@gsm.hs.kr"
                                password = "encoded_password"
                                role = AccountRole.USER
                                this.student = student
                            }
                        every { mockCurrentUserProvider.getCurrentAccount() } returns account
                    }

                    it("동아리 정보를 포함하여 학생 정보를 반환해야 한다") {
                        val result = queryMyInfoService.execute()

                        result.isStudent shouldBe true
                        val studentDto = result.student!!

                        studentDto.majorClub?.id shouldBe 100L
                        studentDto.majorClub?.name shouldBe "SW개발동아리"
                        studentDto.majorClub?.type shouldBe ClubType.MAJOR_CLUB

                        studentDto.autonomousClub?.id shouldBe 300L
                        studentDto.autonomousClub?.name shouldBe "자율동아리"
                        studentDto.autonomousClub?.type shouldBe ClubType.AUTONOMOUS_CLUB
                    }
                }

                context("학번과 기숙사 정보가 없는 학생 계정일 때") {
                    lateinit var account: AccountJpaEntity

                    beforeEach {
                        val student =
                            StudentJpaEntity().apply {
                                id = 30L
                                name = "이영희"
                                sex = Sex.WOMAN
                                email = "lee@gsm.hs.kr"
                                studentNumber = null
                                dormitoryRoomNumber = null
                            }
                        account =
                            AccountJpaEntity().apply {
                                id = 4L
                                email = "lee@gsm.hs.kr"
                                password = "encoded_password"
                                role = AccountRole.USER
                                this.student = student
                            }
                        every { mockCurrentUserProvider.getCurrentAccount() } returns account
                    }

                    it("학번과 기숙사 관련 필드가 null로 반환되어야 한다") {
                        val result = queryMyInfoService.execute()

                        result.isStudent shouldBe true
                        val studentDto = result.student!!
                        studentDto.grade shouldBe null
                        studentDto.classNum shouldBe null
                        studentDto.number shouldBe null
                        studentDto.studentNumber shouldBe null
                        studentDto.dormitoryFloor shouldBe null
                        studentDto.dormitoryRoom shouldBe null
                    }
                }
            }
        }
    })
