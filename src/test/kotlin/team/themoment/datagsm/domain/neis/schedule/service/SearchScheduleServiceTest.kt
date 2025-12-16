package team.themoment.datagsm.domain.neis.schedule.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.domain.neis.schedule.entity.ScheduleRedisEntity
import team.themoment.datagsm.domain.neis.schedule.repository.ScheduleRedisRepository
import team.themoment.datagsm.domain.neis.schedule.service.impl.SearchScheduleServiceImpl
import java.time.LocalDate

class SearchScheduleServiceTest :
    DescribeSpec({

        val mockScheduleRepository = mockk<ScheduleRedisRepository>()
        val searchScheduleService = SearchScheduleServiceImpl(mockScheduleRepository)

        afterEach {
            clearAllMocks()
        }

        describe("SearchScheduleService 클래스의") {
            describe("execute 메서드는") {

                context("특정 날짜로 검색할 때") {
                    val targetDate = LocalDate.of(2025, 12, 16)
                    val schedule =
                        ScheduleRedisEntity(
                            Id = "7380292_20251216",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = targetDate,
                            academicYear = "2025",
                            eventName = "2학기 2차 지필평가",
                            eventContent = "2학기 2차 지필평가",
                            dayCategory = "해당없음",
                            schoolCourseType = "고등학교",
                            dayNightType = "주간",
                            targetGrades = listOf(1, 2, 3),
                        )

                    beforeEach {
                        every { mockScheduleRepository.findAll() } returns listOf(schedule)
                    }

                    it("해당 날짜의 학사일정 정보를 반환해야 한다") {
                        val result = searchScheduleService.execute(date = targetDate, fromDate = null, toDate = null)

                        result.size shouldBe 1
                        result[0].scheduleId shouldBe "7380292_20251216"
                        result[0].scheduleDate shouldBe targetDate
                        result[0].eventName shouldBe "2학기 2차 지필평가"
                        result[0].targetGrades shouldBe listOf(1, 2, 3)

                        verify(exactly = 1) { mockScheduleRepository.findAll() }
                    }
                }

                context("날짜 범위로 검색할 때") {
                    val fromDate = LocalDate.of(2025, 12, 16)
                    val toDate = LocalDate.of(2025, 12, 17)
                    val schedule1 =
                        ScheduleRedisEntity(
                            Id = "7380292_20251216",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = LocalDate.of(2025, 12, 16),
                            academicYear = "2025",
                            eventName = "2학기 2차 지필평가",
                            eventContent = "2학기 2차 지필평가",
                            dayCategory = "해당없음",
                            schoolCourseType = "고등학교",
                            dayNightType = "주간",
                            targetGrades = listOf(1, 2, 3),
                        )
                    val schedule2 =
                        ScheduleRedisEntity(
                            Id = "7380292_20251217",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = LocalDate.of(2025, 12, 17),
                            academicYear = "2025",
                            eventName = "2학기 2차 지필평가",
                            eventContent = "2학기 2차 지필평가",
                            dayCategory = "해당없음",
                            schoolCourseType = "고등학교",
                            dayNightType = "주간",
                            targetGrades = listOf(1, 2, 3),
                        )
                    val schedule3 =
                        ScheduleRedisEntity(
                            Id = "7380292_20251218",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = LocalDate.of(2025, 12, 18),
                            academicYear = "2025",
                            eventName = "종업식",
                            eventContent = "종업식",
                            dayCategory = "해당없음",
                            schoolCourseType = "고등학교",
                            dayNightType = "주간",
                            targetGrades = listOf(1, 2, 3),
                        )

                    beforeEach {
                        every { mockScheduleRepository.findAll() } returns listOf(schedule1, schedule2, schedule3)
                    }

                    it("날짜 범위 내의 학사일정 정보만 반환해야 한다") {
                        val result = searchScheduleService.execute(date = null, fromDate = fromDate, toDate = toDate)

                        result.size shouldBe 2
                        result[0].scheduleDate shouldBe LocalDate.of(2025, 12, 16)
                        result[1].scheduleDate shouldBe LocalDate.of(2025, 12, 17)

                        verify(exactly = 1) { mockScheduleRepository.findAll() }
                    }
                }

                context("검색 결과가 없을 때") {
                    beforeEach {
                        every { mockScheduleRepository.findAll() } returns emptyList()
                    }

                    it("빈 목록을 반환해야 한다") {
                        val result = searchScheduleService.execute(date = LocalDate.now(), fromDate = null, toDate = null)

                        result.size shouldBe 0

                        verify(exactly = 1) { mockScheduleRepository.findAll() }
                    }
                }

                context("파라미터가 모두 null일 때") {
                    val schedule =
                        ScheduleRedisEntity(
                            Id = "7380292_20251216",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = LocalDate.of(2025, 12, 16),
                            academicYear = "2025",
                            eventName = "2학기 2차 지필평가",
                            eventContent = "2학기 2차 지필평가",
                            dayCategory = "해당없음",
                            schoolCourseType = "고등학교",
                            dayNightType = "주간",
                            targetGrades = listOf(1, 2, 3),
                        )

                    beforeEach {
                        every { mockScheduleRepository.findAll() } returns listOf(schedule)
                    }

                    it("모든 학사일정 정보를 반환해야 한다") {
                        val result = searchScheduleService.execute(date = null, fromDate = null, toDate = null)

                        result.size shouldBe 1
                        result[0].scheduleId shouldBe "7380292_20251216"

                        verify(exactly = 1) { mockScheduleRepository.findAll() }
                    }
                }
            }
        }
    })