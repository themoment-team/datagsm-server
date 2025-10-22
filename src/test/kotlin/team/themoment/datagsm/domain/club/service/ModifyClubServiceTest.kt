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
import team.themoment.datagsm.domain.club.service.impl.ModifyClubServiceImpl
import java.util.Optional

class ModifyClubServiceTest :
    DescribeSpec({

        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var modifyClubService: ModifyClubServiceImpl

        beforeEach {
            mockClubRepository = mockk<ClubJpaRepository>()
            modifyClubService = ModifyClubServiceImpl(mockClubRepository)
        }

        describe("ModifyClubService 클래스의") {
            describe("execute 메서드는") {
                val clubId = 1L
                lateinit var existing: ClubJpaEntity

                beforeEach {
                    existing =
                        ClubJpaEntity().apply {
                            this.clubId = clubId
                            clubName = "기존동아리"
                            clubDescription = "기존설명"
                            clubType = ClubType.MAJOR_CLUB
                        }
                }

                context("이름을 다른 값으로 변경할 때") {
                    val req =
                        ClubReqDto(
                            clubName = "새이름",
                            clubDescription = "새설명",
                            clubType = ClubType.JOB_CLUB,
                        )

                    beforeEach {
                        every { mockClubRepository.findById(clubId) } returns Optional.of(existing)
                        every { mockClubRepository.existsByClubNameAndClubIdNot(req.clubName, clubId) } returns false
                        every { mockClubRepository.save(any()) } answers {
                            val e = firstArg<ClubJpaEntity>()
                            e
                        }
                    }

                    it("업데이트된 정보가 반환되어야 한다") {
                        val res = modifyClubService.execute(clubId, req)

                        res.totalPages shouldBe 1
                        res.totalElements shouldBe 1L
                        res.clubs.size shouldBe 1

                        val club = res.clubs[0]
                        club.clubId shouldBe clubId
                        club.clubName shouldBe req.clubName
                        club.clubDescription shouldBe req.clubDescription
                        club.clubType shouldBe req.clubType

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 1) { mockClubRepository.existsByClubNameAndClubIdNot(req.clubName, clubId) }
                        verify(exactly = 1) { mockClubRepository.save(any()) }
                    }
                }

                context("이름을 기존과 동일하게 두고 설명/타입만 변경할 때") {
                    val req =
                        ClubReqDto(
                            clubName = "기존동아리",
                            clubDescription = "변경된설명",
                            clubType = ClubType.AUTONOMOUS_CLUB,
                        )

                    beforeEach {
                        every { mockClubRepository.findById(clubId) } returns Optional.of(existing)
                        every { mockClubRepository.save(any()) } answers { firstArg<ClubJpaEntity>() }
                    }

                    it("중복 이름 검사 없이 저장되어야 한다") {
                        val res = modifyClubService.execute(clubId, req)

                        val club = res.clubs[0]
                        club.clubId shouldBe clubId
                        club.clubName shouldBe req.clubName
                        club.clubDescription shouldBe req.clubDescription
                        club.clubType shouldBe req.clubType

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 0) { mockClubRepository.existsByClubNameAndClubIdNot(any(), any()) }
                        verify(exactly = 1) { mockClubRepository.save(any()) }
                    }
                }

                context("중복된 이름으로 변경 시도할 때") {
                    val req =
                        ClubReqDto(
                            clubName = "기존있는이름",
                            clubDescription = "desc",
                            clubType = ClubType.MAJOR_CLUB,
                        )

                    beforeEach {
                        every { mockClubRepository.findById(clubId) } returns Optional.of(existing)
                        every { mockClubRepository.existsByClubNameAndClubIdNot(req.clubName, clubId) } returns true
                    }

                    it("IllegalArgumentException이 발생해야 한다") {
                        val ex =
                            shouldThrow<IllegalArgumentException> {
                                modifyClubService.execute(clubId, req)
                            }
                        ex.message shouldBe "이미 존재하는 동아리 이름입니다: ${req.clubName}"

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 1) { mockClubRepository.existsByClubNameAndClubIdNot(req.clubName, clubId) }
                        verify(exactly = 0) { mockClubRepository.save(any()) }
                    }
                }

                context("존재하지 않는 동아리 ID로 수정할 때") {
                    val req =
                        ClubReqDto(
                            clubName = "아무이름",
                            clubDescription = "설명",
                            clubType = ClubType.MAJOR_CLUB,
                        )

                    beforeEach {
                        every { mockClubRepository.findById(clubId) } returns Optional.empty()
                    }

                    it("IllegalArgumentException이 발생해야 한다") {
                        val ex =
                            shouldThrow<IllegalArgumentException> {
                                modifyClubService.execute(clubId, req)
                            }
                        ex.message shouldBe "동아리를 찾을 수 없습니다. clubId: $clubId"

                        verify(exactly = 1) { mockClubRepository.findById(clubId) }
                        verify(exactly = 0) { mockClubRepository.save(any()) }
                    }
                }
            }
        }
    })
