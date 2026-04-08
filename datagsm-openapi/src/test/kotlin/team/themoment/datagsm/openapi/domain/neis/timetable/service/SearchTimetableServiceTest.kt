package team.themoment.datagsm.openapi.domain.neis.timetable.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.neis.dto.timetable.request.QueryTimetableReqDto
import team.themoment.datagsm.common.domain.neis.timetable.entity.TimetableRedisEntity
import team.themoment.datagsm.common.domain.neis.timetable.repository.TimetableRedisRepository
import team.themoment.datagsm.openapi.domain.neis.timetable.service.impl.SearchTimetableServiceImpl
import java.time.LocalDate

class SearchTimetableServiceTest :
    DescribeSpec({

        val mockTimetableRepository = mockk<TimetableRedisRepository>()
        val searchTimetableService = SearchTimetableServiceImpl(mockTimetableRepository)

        afterEach {
            clearAllMocks()
        }

        describe("SearchTimetableService 클래스의") {
            describe("execute 메서드는") {

                context("특정 날짜로 검색할 때") {
                    val targetDate = LocalDate.of(2025, 4, 1)
                    val timetable1 =
                        TimetableRedisEntity(
                            id = "7380292_20250401_1_1_1",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = targetDate,
                            academicYear = "2025",
                            semester = "1",
                            grade = 1,
                            classNum = 1,
                            period = 1,
                            subject = "국어",
                        )
                    val timetable2 =
                        TimetableRedisEntity(
                            id = "7380292_20250401_1_1_2",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = targetDate,
                            academicYear = "2025",
                            semester = "1",
                            grade = 1,
                            classNum = 1,
                            period = 2,
                            subject = "수학",
                        )

                    beforeEach {
                        every {
                            mockTimetableRepository.findByGradeAndClassNumAndDate(1, 1, targetDate)
                        } returns listOf(timetable1, timetable2)
                    }

                    it("해당 날짜의 시간표 정보를 반환해야 한다") {
                        val result =
                            searchTimetableService.execute(
                                QueryTimetableReqDto(grade = 1, classNum = 1, date = targetDate),
                            )

                        result.timetables.size shouldBe 2
                        result.timetables[0].timetableId shouldBe "7380292_20250401_1_1_1"
                        result.timetables[0].grade shouldBe 1
                        result.timetables[0].classNum shouldBe 1
                        result.timetables[0].period shouldBe 1
                        result.timetables[0].subject shouldBe "국어"
                        result.timetables[1].timetableId shouldBe "7380292_20250401_1_1_2"
                        result.timetables[1].period shouldBe 2

                        verify(exactly = 1) { mockTimetableRepository.findByGradeAndClassNumAndDate(1, 1, targetDate) }
                    }
                }

                context("날짜 범위로 검색할 때") {
                    val startDate = LocalDate.of(2025, 4, 1)
                    val endDate = LocalDate.of(2025, 4, 2)
                    val timetable1 =
                        TimetableRedisEntity(
                            id = "7380292_20250401_1_1_1",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = startDate,
                            academicYear = "2025",
                            semester = "1",
                            grade = 1,
                            classNum = 1,
                            period = 1,
                            subject = "국어",
                        )
                    val timetable2 =
                        TimetableRedisEntity(
                            id = "7380292_20250402_1_1_1",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = endDate,
                            academicYear = "2025",
                            semester = "1",
                            grade = 1,
                            classNum = 1,
                            period = 1,
                            subject = "영어",
                        )

                    beforeEach {
                        every {
                            mockTimetableRepository.findByGradeAndClassNumAndDateBetween(1, 1, startDate, endDate)
                        } returns listOf(timetable1, timetable2)
                    }

                    it("날짜 범위 내의 시간표 정보를 반환해야 한다") {
                        val result =
                            searchTimetableService.execute(
                                QueryTimetableReqDto(grade = 1, classNum = 1, startDate = startDate, endDate = endDate),
                            )

                        result.timetables.size shouldBe 2
                        result.timetables[0].timetableDate shouldBe startDate
                        result.timetables[1].timetableDate shouldBe endDate

                        verify(exactly = 1) {
                            mockTimetableRepository.findByGradeAndClassNumAndDateBetween(1, 1, startDate, endDate)
                        }
                    }
                }

                context("startDate만 지정하여 검색할 때") {
                    val startDate = LocalDate.of(2025, 4, 1)
                    val timetable =
                        TimetableRedisEntity(
                            id = "7380292_20250401_1_1_1",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = startDate,
                            academicYear = "2025",
                            semester = "1",
                            grade = 1,
                            classNum = 1,
                            period = 1,
                            subject = "국어",
                        )

                    beforeEach {
                        every {
                            mockTimetableRepository.findByGradeAndClassNumAndDateGreaterThanEqual(1, 1, startDate)
                        } returns listOf(timetable)
                    }

                    it("startDate 이후의 시간표 정보를 반환해야 한다") {
                        val result =
                            searchTimetableService.execute(
                                QueryTimetableReqDto(grade = 1, classNum = 1, startDate = startDate),
                            )

                        result.timetables.size shouldBe 1
                        result.timetables[0].timetableDate shouldBe startDate

                        verify(exactly = 1) {
                            mockTimetableRepository.findByGradeAndClassNumAndDateGreaterThanEqual(1, 1, startDate)
                        }
                    }
                }

                context("endDate만 지정하여 검색할 때") {
                    val endDate = LocalDate.of(2025, 4, 30)
                    val timetable =
                        TimetableRedisEntity(
                            id = "7380292_20250430_1_1_1",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = endDate,
                            academicYear = "2025",
                            semester = "1",
                            grade = 1,
                            classNum = 1,
                            period = 1,
                            subject = "국어",
                        )

                    beforeEach {
                        every {
                            mockTimetableRepository.findByGradeAndClassNumAndDateLessThanEqual(1, 1, endDate)
                        } returns listOf(timetable)
                    }

                    it("endDate 이전의 시간표 정보를 반환해야 한다") {
                        val result =
                            searchTimetableService.execute(
                                QueryTimetableReqDto(grade = 1, classNum = 1, endDate = endDate),
                            )

                        result.timetables.size shouldBe 1
                        result.timetables[0].timetableDate shouldBe endDate

                        verify(exactly = 1) {
                            mockTimetableRepository.findByGradeAndClassNumAndDateLessThanEqual(1, 1, endDate)
                        }
                    }
                }

                context("검색 결과가 없을 때") {
                    val targetDate = LocalDate.of(2025, 4, 1)

                    beforeEach {
                        every {
                            mockTimetableRepository.findByGradeAndClassNumAndDate(1, 1, targetDate)
                        } returns emptyList()
                    }

                    it("빈 목록을 반환해야 한다") {
                        val result =
                            searchTimetableService.execute(
                                QueryTimetableReqDto(grade = 1, classNum = 1, date = targetDate),
                            )

                        result.timetables.size shouldBe 0

                        verify(exactly = 1) { mockTimetableRepository.findByGradeAndClassNumAndDate(1, 1, targetDate) }
                    }
                }
            }
        }
    })
