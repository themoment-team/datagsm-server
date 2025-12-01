package team.themoment.datagsm.domain.club.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.club.service.impl.QueryClubServiceImpl

class QueryClubServiceTest :
    DescribeSpec({

        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var queryClubService: QueryClubService

        beforeEach {
            mockClubRepository = mockk<ClubJpaRepository>()
            queryClubService = QueryClubServiceImpl(mockClubRepository)
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
                                clubId = clubId,
                                clubName = clubName,
                                clubType = clubType,
                                pageable = any(),
                            )
                        } returns PageImpl(emptyList())
                    }

                    it("빈 목록과 0 카운트를 반환해야 한다") {
                        val res = queryClubService.execute(clubId, clubName, clubType, page, size)

                        res.totalPages shouldBe 1 // PageImpl(emptyList())의 totalPages는 1로 계산됨
                        res.totalElements shouldBe 0
                        res.clubs.size shouldBe 0

                        verify(exactly = 1) {
                            mockClubRepository.searchClubWithPaging(clubId, clubName, clubType, any())
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
                    beforeEach {
                        e1 =
                            ClubJpaEntity().apply {
                                this.id = 1L
                                this.name = "A"
                                this.type = ClubType.MAJOR_CLUB
                            }
                        e2 =
                            ClubJpaEntity().apply {
                                this.id = 2L
                                this.name = "B"
                                this.type = ClubType.MAJOR_CLUB
                            }
                        every {
                            mockClubRepository.searchClubWithPaging(
                                clubId = clubId,
                                clubName = clubName,
                                clubType = clubType,
                                pageable = any(),
                            )
                        } returns PageImpl(listOf(e1, e2))
                    }

                    it("조회된 엔티티들을 DTO로 매핑해 반환해야 한다") {
                        val res = queryClubService.execute(clubId, clubName, clubType, page, size)

                        res.clubs.size shouldBe 2
                        res.clubs[0].clubId shouldBe 1L
                        res.clubs[0].clubName shouldBe "A"
                        res.clubs[0].clubType shouldBe ClubType.MAJOR_CLUB

                        res.clubs[1].clubId shouldBe 2L
                        res.clubs[1].clubName shouldBe "B"
                        res.clubs[1].clubType shouldBe ClubType.MAJOR_CLUB

                        verify(exactly = 1) {
                            mockClubRepository.searchClubWithPaging(clubId, clubName, clubType, any())
                        }
                    }
                }

                context("Pageable로 검색하고 결과가 있을 때") {
                    lateinit var e1: ClubJpaEntity
                    lateinit var e2: ClubJpaEntity
                    lateinit var e3: ClubJpaEntity
                    lateinit var e4: ClubJpaEntity
                    lateinit var e5: ClubJpaEntity

                    beforeEach {
                        e1 =
                            ClubJpaEntity().apply {
                                this.id = 1L
                                this.name = "A"
                                this.type = ClubType.MAJOR_CLUB
                            }
                        e2 =
                            ClubJpaEntity().apply {
                                this.id = 2L
                                this.name = "B"
                                this.type = ClubType.MAJOR_CLUB
                            }
                        e3 =
                            ClubJpaEntity().apply {
                                this.id = 3L
                                this.name = "C"
                                this.type = ClubType.MAJOR_CLUB
                            }
                        e4 =
                            ClubJpaEntity().apply {
                                this.id = 4L
                                this.name = "D"
                                this.type = ClubType.MAJOR_CLUB
                            }
                        e5 =
                            ClubJpaEntity().apply {
                                this.id = 5L
                                this.name = "E"
                                this.type = ClubType.MAJOR_CLUB
                            }

                        every { mockClubRepository.searchClubWithPaging(any(), any(), any(), any()) } answers {
                            val pageable = arg<Pageable>(3)
                            val all = listOf(e1, e2, e3, e4, e5)
                            val start = pageable.offset.toInt()
                            val end = kotlin.math.min(start + pageable.pageSize, all.size)
                            val content = if (start >= all.size) emptyList() else all.subList(start, end)
                            PageImpl(content, pageable, all.size.toLong())
                        }
                    }

                    it("알맞은 Page 결과가 반환된다") {
                        val res = queryClubService.execute(null, null, null, 1, 2)

                        res.totalElements shouldBe 5
                        res.totalPages shouldBe 3

                        res.clubs.size shouldBe 2
                        res.clubs[0].clubId shouldBe 3L
                        res.clubs[0].clubName shouldBe "C"
                        res.clubs[0].clubType shouldBe ClubType.MAJOR_CLUB

                        res.clubs[1].clubId shouldBe 4L
                        res.clubs[1].clubName shouldBe "D"
                        res.clubs[1].clubType shouldBe ClubType.MAJOR_CLUB

                        verify(exactly = 1) { mockClubRepository.searchClubWithPaging(null, null, null, any()) }
                    }
                }
            }
        }
    })
