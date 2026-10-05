package team.themoment.datagsm.web.domain.club.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.domain.Sort
import team.themoment.datagsm.common.domain.club.dto.request.QueryPublicClubReqDto
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.web.domain.club.service.impl.QueryPublicClubServiceImpl

class QueryPublicClubServiceTest :
    DescribeSpec({

        val mockClubRepository = mockk<ClubJpaRepository>()
        val queryPublicClubService = QueryPublicClubServiceImpl(mockClubRepository)

        fun club(
            clubId: Long,
            clubName: String,
            clubType: ClubType,
            clubStatus: ClubStatus,
        ) = ClubJpaEntity().apply {
            id = clubId
            name = clubName
            type = clubType
            status = clubStatus
        }

        val activeMajorClub = club(1L, "SW개발동아리", ClubType.MAJOR_CLUB, ClubStatus.ACTIVE)
        val abolishedMajorClub = club(2L, "폐지된동아리", ClubType.MAJOR_CLUB, ClubStatus.ABOLISHED)
        val activeAutonomousClub = club(3L, "자율동아리", ClubType.AUTONOMOUS_CLUB, ClubStatus.ACTIVE)

        beforeEach {
            every { mockClubRepository.findAll(any<Sort>()) } returns
                listOf(activeMajorClub, abolishedMajorClub, activeAutonomousClub)
        }

        afterEach {
            clearAllMocks()
        }

        describe("QueryPublicClubService 클래스의") {

            describe("execute 메서드는") {

                context("필터 조건이 없을 때") {
                    it("전체 동아리의 요약 정보가 반환되어야 한다") {
                        val result = queryPublicClubService.execute(QueryPublicClubReqDto())

                        result.clubs.map { it.id } shouldContainExactly listOf(1L, 2L, 3L)
                        result.clubs[0].name shouldBe "SW개발동아리"
                        result.clubs[0].type shouldBe ClubType.MAJOR_CLUB
                    }
                }

                context("동아리 종류가 주어질 때") {
                    it("해당 종류의 동아리만 반환되어야 한다") {
                        val result =
                            queryPublicClubService.execute(QueryPublicClubReqDto(clubType = ClubType.MAJOR_CLUB))

                        result.clubs.map { it.id } shouldContainExactly listOf(1L, 2L)
                    }
                }

                context("동아리 종류와 운영 상태가 함께 주어질 때") {
                    it("두 조건을 모두 만족하는 동아리만 반환되어야 한다") {
                        val result =
                            queryPublicClubService.execute(
                                QueryPublicClubReqDto(clubType = ClubType.MAJOR_CLUB, clubStatus = ClubStatus.ACTIVE),
                            )

                        result.clubs.map { it.id } shouldContainExactly listOf(1L)
                    }
                }
            }
        }
    })
