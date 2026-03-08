package team.themoment.datagsm.openapi.domain.club.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import team.themoment.datagsm.common.domain.club.dto.request.QueryClubReqDto
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.openapi.domain.club.service.impl.QueryClubServiceImpl

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
                    beforeEach {
                        every {
                            mockClubRepository.searchClubWithPaging(
                                id = null,
                                name = null,
                                type = null,
                                pageable = any(),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(emptyList())
                    }

                    it("빈 목록과 0 카운트를 반환해야 한다") {
                        val queryReq = QueryClubReqDto()
                        val result = queryClubService.execute(queryReq)

                        result.totalElements shouldBe 0
                        result.clubs.size shouldBe 0

                        verify(exactly = 1) {
                            mockClubRepository.searchClubWithPaging(null, null, null, any(), any(), any())
                        }
                    }
                }

                context("includeLeaderInParticipants=false일 때") {
                    lateinit var leader: StudentJpaEntity
                    lateinit var member: StudentJpaEntity
                    lateinit var club: ClubJpaEntity

                    beforeEach {
                        leader =
                            StudentJpaEntity().apply {
                                id = 10L
                                name = "부장"
                                email = "leader@gsm.hs.kr"
                                studentNumber = StudentNumber(1, 1, 1)
                                major = Major.SW_DEVELOPMENT
                                sex = Sex.MAN
                            }
                        member =
                            StudentJpaEntity().apply {
                                id = 11L
                                name = "부원"
                                email = "member@gsm.hs.kr"
                                studentNumber = StudentNumber(1, 1, 2)
                                major = Major.SW_DEVELOPMENT
                                sex = Sex.WOMAN
                            }
                        club =
                            ClubJpaEntity().apply {
                                id = 1L
                                name = "SW개발동아리"
                                type = ClubType.MAJOR_CLUB
                                this.leader = leader
                            }
                        every {
                            mockClubRepository.searchClubWithPaging(
                                id = null,
                                name = null,
                                type = ClubType.MAJOR_CLUB,
                                pageable = any(),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(club))
                        every { mockStudentRepository.findRegisteredStudentsByMajorClub(club) } returns listOf(leader, member)
                    }

                    it("부장이 부원 목록에서 제외되어야 한다") {
                        val queryReq =
                            QueryClubReqDto(
                                clubType = ClubType.MAJOR_CLUB,
                                includeLeaderInParticipants = false,
                            )
                        val result = queryClubService.execute(queryReq)

                        result.clubs.size shouldBe 1
                        val clubDto = result.clubs[0]
                        clubDto.leader.id shouldBe 10L
                        clubDto.participants.size shouldBe 1
                        clubDto.participants[0].id shouldBe 11L
                    }
                }

                context("includeLeaderInParticipants=true일 때") {
                    lateinit var leader: StudentJpaEntity
                    lateinit var member: StudentJpaEntity
                    lateinit var club: ClubJpaEntity

                    beforeEach {
                        leader =
                            StudentJpaEntity().apply {
                                id = 10L
                                name = "부장"
                                email = "leader@gsm.hs.kr"
                                studentNumber = StudentNumber(1, 1, 1)
                                major = Major.SW_DEVELOPMENT
                                sex = Sex.MAN
                            }
                        member =
                            StudentJpaEntity().apply {
                                id = 11L
                                name = "부원"
                                email = "member@gsm.hs.kr"
                                studentNumber = StudentNumber(1, 1, 2)
                                major = Major.SW_DEVELOPMENT
                                sex = Sex.WOMAN
                            }
                        club =
                            ClubJpaEntity().apply {
                                id = 1L
                                name = "SW개발동아리"
                                type = ClubType.MAJOR_CLUB
                                this.leader = leader
                            }
                        every {
                            mockClubRepository.searchClubWithPaging(
                                id = null,
                                name = null,
                                type = ClubType.MAJOR_CLUB,
                                pageable = any(),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(club))
                        every { mockStudentRepository.findRegisteredStudentsByMajorClub(club) } returns listOf(leader, member)
                    }

                    it("부장이 부원 목록에 포함되어야 한다") {
                        val queryReq =
                            QueryClubReqDto(
                                clubType = ClubType.MAJOR_CLUB,
                                includeLeaderInParticipants = true,
                            )
                        val result = queryClubService.execute(queryReq)

                        result.clubs.size shouldBe 1
                        val clubDto = result.clubs[0]
                        clubDto.leader.id shouldBe 10L
                        clubDto.participants.size shouldBe 2
                        clubDto.participants.map { it.id } shouldBe listOf(10L, 11L)
                    }
                }

                context("AUTONOMOUS_CLUB 타입의 동아리를 조회할 때") {
                    lateinit var leader: StudentJpaEntity
                    lateinit var club: ClubJpaEntity

                    beforeEach {
                        leader =
                            StudentJpaEntity().apply {
                                id = 30L
                                name = "자율부장"
                                email = "auto_leader@gsm.hs.kr"
                                studentNumber = StudentNumber(2, 3, 5)
                                major = Major.AI
                                sex = Sex.MAN
                            }
                        club =
                            ClubJpaEntity().apply {
                                id = 3L
                                name = "자율동아리"
                                type = ClubType.AUTONOMOUS_CLUB
                                this.leader = leader
                            }
                        every {
                            mockClubRepository.searchClubWithPaging(
                                id = null,
                                name = null,
                                type = ClubType.AUTONOMOUS_CLUB,
                                pageable = any(),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(club))
                        every { mockStudentRepository.findRegisteredStudentsByAutonomousClub(club) } returns listOf(leader)
                    }

                    it("AUTONOMOUS_CLUB 타입의 동아리 정보를 정상 반환해야 한다") {
                        val queryReq = QueryClubReqDto(clubType = ClubType.AUTONOMOUS_CLUB)
                        val result = queryClubService.execute(queryReq)

                        result.clubs.size shouldBe 1
                        result.clubs[0].id shouldBe 3L
                        result.clubs[0].name shouldBe "자율동아리"
                        result.clubs[0].type shouldBe ClubType.AUTONOMOUS_CLUB
                    }
                }

                context("동아리 이름으로 검색할 때") {
                    lateinit var leader: StudentJpaEntity
                    lateinit var club: ClubJpaEntity

                    beforeEach {
                        leader =
                            StudentJpaEntity().apply {
                                id = 40L
                                name = "부장이름"
                                email = "leader2@gsm.hs.kr"
                                studentNumber = StudentNumber(1, 2, 3)
                                major = Major.SW_DEVELOPMENT
                                sex = Sex.MAN
                            }
                        club =
                            ClubJpaEntity().apply {
                                id = 4L
                                name = "검색동아리"
                                type = ClubType.MAJOR_CLUB
                                this.leader = leader
                            }
                        every {
                            mockClubRepository.searchClubWithPaging(
                                id = null,
                                name = "검색동아리",
                                type = null,
                                pageable = any(),
                                sortBy = any(),
                                sortDirection = any(),
                            )
                        } returns PageImpl(listOf(club))
                        every { mockStudentRepository.findRegisteredStudentsByMajorClub(club) } returns listOf(leader)
                    }

                    it("이름에 해당하는 동아리 정보가 반환되어야 한다") {
                        val queryReq = QueryClubReqDto(clubName = "검색동아리")
                        val result = queryClubService.execute(queryReq)

                        result.clubs.size shouldBe 1
                        result.clubs[0].id shouldBe 4L
                        result.clubs[0].name shouldBe "검색동아리"

                        verify(exactly = 1) {
                            mockClubRepository.searchClubWithPaging(null, "검색동아리", null, any(), any(), any())
                        }
                    }
                }
            }
        }
    })
