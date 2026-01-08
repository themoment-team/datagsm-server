package team.themoment.datagsm.web.domain.club.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.common.domain.club.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.ClubType
import team.themoment.datagsm.web.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.web.domain.club.service.impl.QueryClubServiceImpl
import team.themoment.datagsm.common.domain.student.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.Major
import team.themoment.datagsm.common.domain.student.Sex
import team.themoment.datagsm.common.domain.student.StudentNumber
import team.themoment.datagsm.web.domain.student.repository.StudentJpaRepository

class QueryClubServiceTest :
    DescribeSpec({

        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var mockStudentRepository: StudentJpaRepository
        lateinit var queryClubService: QueryClubService

        beforeEach {
            mockClubRepository = mockk<ClubJpaRepository>()
            mockStudentRepository = mockk<StudentJpaRepository>()
            queryClubService = QueryClubServiceImpl(mockClubRepository, mockStudentRepository)
        }

        describe("QueryClubService 클래스의") {
            describe("execute 메서드는") {
                context("검색 결과가 없을 때") {
                    val clubId: Long? = null
                    val clubName: String? = null
                    val clubType: ClubType? = null
                    val page = 0
                    val size = 10

                    beforeEach {
                        every {
                            mockClubRepository.searchClubWithPaging(
                                id = clubId,
                                name = clubName,
                                type = clubType,
                                pageable = any(),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(emptyList())
                    }

                    it("빈 목록과 0 카운트를 반환해야 한다") {
                        val res = queryClubService.execute(clubId, clubName, clubType, page, size)

                        res.totalPages shouldBe 1 // PageImpl(emptyList())의 totalPages는 1로 계산됨
                        res.totalElements shouldBe 0
                        res.clubs.size shouldBe 0

                        verify(exactly = 1) {
                            mockClubRepository.searchClubWithPaging(clubId, clubName, clubType, any(), any(), any())
                        }
                    }
                }

                context("검색 결과가 있을 때") {
                    val clubId: Long? = null
                    val clubName: String = "test"
                    val clubType: ClubType = ClubType.MAJOR_CLUB
                    val page = 1
                    val size = 2

                    lateinit var e1: ClubJpaEntity
                    lateinit var e2: ClubJpaEntity
                    lateinit var leader1: StudentJpaEntity
                    lateinit var leader2: StudentJpaEntity
                    beforeEach {
                        leader1 =
                            StudentJpaEntity().apply {
                                this.id = 10L
                                this.name = "Leader1"
                                this.email = "leader1@gsm.hs.kr"
                                this.studentNumber = StudentNumber(1, 2, 1)
                                this.major = Major.SW_DEVELOPMENT
                                this.sex = Sex.MAN
                            }
                        leader2 =
                            StudentJpaEntity().apply {
                                this.id = 20L
                                this.name = "Leader2"
                                this.email = "leader2@gsm.hs.kr"
                                this.studentNumber = StudentNumber(2, 3, 2)
                                this.major = Major.AI
                                this.sex = Sex.WOMAN
                            }
                        e1 =
                            ClubJpaEntity().apply {
                                this.id = 1L
                                this.name = "A"
                                this.type = ClubType.MAJOR_CLUB
                                this.leader = leader1
                            }
                        e2 =
                            ClubJpaEntity().apply {
                                this.id = 2L
                                this.name = "B"
                                this.type = ClubType.MAJOR_CLUB
                                this.leader = leader2
                            }
                        every {
                            mockClubRepository.searchClubWithPaging(
                                id = clubId,
                                name = clubName,
                                type = clubType,
                                pageable = any(),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(e1, e2))
                        every { mockStudentRepository.findByMajorClub(e1) } returns emptyList()
                        every { mockStudentRepository.findByMajorClub(e2) } returns emptyList()
                    }

                    it("조회된 엔티티들을 DTO로 매핑해 반환해야 한다") {
                        val res = queryClubService.execute(clubId, clubName, clubType, page, size)

                        res.clubs.size shouldBe 2
                        res.clubs[0].id shouldBe 1L
                        res.clubs[0].name shouldBe "A"
                        res.clubs[0].type shouldBe ClubType.MAJOR_CLUB
                        res.clubs[0].leader.id shouldBe 10L
                        res.clubs[0].leader.name shouldBe "Leader1"

                        res.clubs[1].id shouldBe 2L
                        res.clubs[1].name shouldBe "B"
                        res.clubs[1].type shouldBe ClubType.MAJOR_CLUB
                        res.clubs[1].leader.id shouldBe 20L
                        res.clubs[1].leader.name shouldBe "Leader2"

                        verify(exactly = 1) {
                            mockClubRepository.searchClubWithPaging(clubId, clubName, clubType, any(), any(), any())
                        }
                    }
                }

                context("Pageable로 검색하고 결과가 있을 때") {
                    lateinit var e1: ClubJpaEntity
                    lateinit var e2: ClubJpaEntity
                    lateinit var e3: ClubJpaEntity
                    lateinit var e4: ClubJpaEntity
                    lateinit var e5: ClubJpaEntity
                    lateinit var leader1: StudentJpaEntity
                    lateinit var leader2: StudentJpaEntity
                    lateinit var leader3: StudentJpaEntity
                    lateinit var leader4: StudentJpaEntity
                    lateinit var leader5: StudentJpaEntity

                    beforeEach {
                        leader1 =
                            StudentJpaEntity().apply {
                                this.id = 10L
                                this.name = "L1"
                                this.email = "l1@gsm.hs.kr"
                                this.studentNumber = StudentNumber(1, 1, 1)
                                this.major = Major.SW_DEVELOPMENT
                                this.sex = Sex.MAN
                            }
                        leader2 =
                            StudentJpaEntity().apply {
                                this.id = 20L
                                this.name = "L2"
                                this.email = "l2@gsm.hs.kr"
                                this.studentNumber = StudentNumber(1, 1, 2)
                                this.major = Major.SW_DEVELOPMENT
                                this.sex = Sex.MAN
                            }
                        leader3 =
                            StudentJpaEntity().apply {
                                this.id = 30L
                                this.name = "L3"
                                this.email = "l3@gsm.hs.kr"
                                this.studentNumber = StudentNumber(1, 1, 3)
                                this.major = Major.SW_DEVELOPMENT
                                this.sex = Sex.MAN
                            }
                        leader4 =
                            StudentJpaEntity().apply {
                                this.id = 40L
                                this.name = "L4"
                                this.email = "l4@gsm.hs.kr"
                                this.studentNumber = StudentNumber(1, 1, 4)
                                this.major = Major.SW_DEVELOPMENT
                                this.sex = Sex.MAN
                            }
                        leader5 =
                            StudentJpaEntity().apply {
                                this.id = 50L
                                this.name = "L5"
                                this.email = "l5@gsm.hs.kr"
                                this.studentNumber = StudentNumber(1, 1, 5)
                                this.major = Major.SW_DEVELOPMENT
                                this.sex = Sex.MAN
                            }
                        e1 =
                            ClubJpaEntity().apply {
                                this.id = 1L
                                this.name = "A"
                                this.type = ClubType.MAJOR_CLUB
                                this.leader = leader1
                            }
                        e2 =
                            ClubJpaEntity().apply {
                                this.id = 2L
                                this.name = "B"
                                this.type = ClubType.MAJOR_CLUB
                                this.leader = leader2
                            }
                        e3 =
                            ClubJpaEntity().apply {
                                this.id = 3L
                                this.name = "C"
                                this.type = ClubType.MAJOR_CLUB
                                this.leader = leader3
                            }
                        e4 =
                            ClubJpaEntity().apply {
                                this.id = 4L
                                this.name = "D"
                                this.type = ClubType.MAJOR_CLUB
                                this.leader = leader4
                            }
                        e5 =
                            ClubJpaEntity().apply {
                                this.id = 5L
                                this.name = "E"
                                this.type = ClubType.MAJOR_CLUB
                                this.leader = leader5
                            }

                        every { mockClubRepository.searchClubWithPaging(any(), any(), any(), any(), any(), any()) } answers {
                            val pageable = arg<Pageable>(3)
                            val all = listOf(e1, e2, e3, e4, e5)
                            val start = pageable.offset.toInt()
                            val end = kotlin.math.min(start + pageable.pageSize, all.size)
                            val content = if (start >= all.size) emptyList() else all.subList(start, end)
                            PageImpl(content, pageable, all.size.toLong())
                        }
                        every { mockStudentRepository.findByMajorClub(any()) } returns emptyList()
                    }

                    it("알맞은 Page 결과가 반환된다") {
                        val res = queryClubService.execute(null, null, null, 1, 2)

                        res.totalElements shouldBe 5
                        res.totalPages shouldBe 3

                        res.clubs.size shouldBe 2
                        res.clubs[0].id shouldBe 3L
                        res.clubs[0].name shouldBe "C"
                        res.clubs[0].type shouldBe ClubType.MAJOR_CLUB
                        res.clubs[0].leader.id shouldBe 30L

                        res.clubs[1].id shouldBe 4L
                        res.clubs[1].name shouldBe "D"
                        res.clubs[1].type shouldBe ClubType.MAJOR_CLUB
                        res.clubs[1].leader.id shouldBe 40L

                        verify(exactly = 1) { mockClubRepository.searchClubWithPaging(null, null, null, any(), any(), any()) }
                    }
                }
            }
        }
    })
