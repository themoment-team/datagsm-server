package team.themoment.datagsm.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.club.service.impl.CreateClubServiceImpl

class CreateClubServiceTest :
    DescribeSpec({

        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var createClubService: CreateClubService

        beforeEach {
            mockClubRepository = mockk<ClubJpaRepository>()
            createClubService = CreateClubServiceImpl(mockClubRepository)
        }

        describe("CreateClubService 클래스의") {
            describe("execute 메서드는") {
                context("중복된 동아리 이름으로 생성할 때") {
                    val req =
                        ClubReqDto(
                            clubName = "동아리A",
                            clubDescription = "설명",
                            clubType = ClubType.MAJOR_CLUB,
                        )

                    beforeEach {
                        every { mockClubRepository.existsByClubName(req.clubName) } returns true
                    }

                    it("IllegalArgumentException이 발생해야 한다") {
                        val ex =
                            shouldThrow<IllegalArgumentException> {
                                createClubService.execute(req)
                            }
                        ex.message shouldBe "이미 존재하는 동아리 이름입니다: ${req.clubName}"

                        verify(exactly = 1) { mockClubRepository.existsByClubName(req.clubName) }
                        verify(exactly = 0) { mockClubRepository.save(any()) }
                    }
                }

                context("정상적으로 동아리를 생성할 때") {
                    val req =
                        ClubReqDto(
                            clubName = "동아리B",
                            clubDescription = "B 설명",
                            clubType = ClubType.AUTONOMOUS_CLUB,
                        )

                    beforeEach {
                        every { mockClubRepository.existsByClubName(req.clubName) } returns false
                        every { mockClubRepository.save(any()) } answers {
                            val entity = firstArg<ClubJpaEntity>()
                            entity.apply { this.clubId = 10L }
                        }
                    }

                    it("생성된 동아리 정보를 반환해야 한다") {
                        val res = createClubService.execute(req)

                        res.clubName shouldBe req.clubName
                        res.clubDescription shouldBe req.clubDescription
                        res.clubType shouldBe req.clubType

                        verify(exactly = 1) { mockClubRepository.existsByClubName(req.clubName) }
                        verify(exactly = 1) { mockClubRepository.save(any()) }
                    }
                }
            }
        }
    })
