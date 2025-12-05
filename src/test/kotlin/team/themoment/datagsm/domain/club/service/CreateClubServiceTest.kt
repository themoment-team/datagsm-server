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
import team.themoment.datagsm.global.exception.error.ExpectedException

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
                            name = "동아리A",
                            type = ClubType.MAJOR_CLUB,
                        )

                    beforeEach {
                        every { mockClubRepository.existsByName(req.name) } returns true
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val ex =
                            shouldThrow<ExpectedException> {
                                createClubService.execute(req)
                            }
                        ex.message shouldBe "이미 존재하는 동아리 이름입니다: ${req.name}"

                        verify(exactly = 1) { mockClubRepository.existsByName(req.name) }
                        verify(exactly = 0) { mockClubRepository.save(any()) }
                    }
                }

                context("정상적으로 동아리를 생성할 때") {
                    val req =
                        ClubReqDto(
                            name = "동아리B",
                            type = ClubType.AUTONOMOUS_CLUB,
                        )

                    beforeEach {
                        every { mockClubRepository.existsByName(req.name) } returns false
                        every { mockClubRepository.save(any()) } answers {
                            val entity = firstArg<ClubJpaEntity>()
                            entity.apply { this.id = 10L }
                        }
                    }

                    it("생성된 동아리 정보를 반환해야 한다") {
                        val res = createClubService.execute(req)

                        res.name shouldBe req.name
                        res.type shouldBe req.type

                        verify(exactly = 1) { mockClubRepository.existsByName(req.name) }
                        verify(exactly = 1) { mockClubRepository.save(any()) }
                    }
                }
            }
        }
    })
