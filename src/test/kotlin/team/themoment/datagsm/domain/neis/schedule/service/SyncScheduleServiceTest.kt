package team.themoment.datagsm.domain.neis.schedule.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import team.themoment.datagsm.domain.neis.schedule.entity.ScheduleRedisEntity
import team.themoment.datagsm.domain.neis.schedule.repository.ScheduleRedisRepository
import team.themoment.datagsm.domain.neis.schedule.service.impl.SyncScheduleServiceImpl
import team.themoment.datagsm.global.config.neis.NeisEnvironment
import team.themoment.datagsm.global.thirdparty.feign.neis.NeisApiClient
import team.themoment.datagsm.global.thirdparty.feign.neis.dto.NeisScheduleApiResponse
import team.themoment.datagsm.global.thirdparty.feign.neis.dto.SchoolScheduleInfo
import team.themoment.datagsm.global.thirdparty.feign.neis.dto.SchoolScheduleWrapper
import java.time.LocalDate

class SyncScheduleServiceTest :
    DescribeSpec({

        val mockNeisApiClient = mockk<NeisApiClient>()
        val mockScheduleRepository = mockk<ScheduleRedisRepository>()
        val mockNeisEnvironment =
            mockk<NeisEnvironment>().apply {
                every { key } returns "test-api-key"
                every { officeCode } returns "F10"
                every { schoolCode } returns "7380292"
            }

        val syncScheduleService =
            SyncScheduleServiceImpl(
                mockNeisApiClient,
                mockScheduleRepository,
                mockNeisEnvironment,
            )

        afterEach {
            clearAllMocks()
        }

        describe("SyncScheduleService 클래스의") {
            describe("execute 메서드는") {

                context("NEIS API로부터 정상적으로 데이터를 받아올 때") {
                    val fromDate = LocalDate.of(2025, 12, 16)
                    val toDate = LocalDate.of(2025, 12, 17)

                    val scheduleInfo1 =
                        SchoolScheduleInfo(
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            academicYear = "2025",
                            scheduleDate = "20251216",
                            eventName = "2학기 2차 지필평가",
                            eventContent = "2학기 2차 지필평가",
                            dayCategory = "해당없음",
                            grade1EventYn = "Y",
                            grade2EventYn = "Y",
                            grade3EventYn = "Y",
                            grade4EventYn = null,
                            grade5EventYn = null,
                            grade6EventYn = null,
                            schoolCourseType = "고등학교",
                            dayNightType = "주간",
                            loadDateTime = null,
                        )
                    val scheduleInfo2 =
                        SchoolScheduleInfo(
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            academicYear = "2025",
                            scheduleDate = "20251217",
                            eventName = "2학기 2차 지필평가",
                            eventContent = "2학기 2차 지필평가",
                            dayCategory = "해당없음",
                            grade1EventYn = "Y",
                            grade2EventYn = "Y",
                            grade3EventYn = "Y",
                            grade4EventYn = null,
                            grade5EventYn = null,
                            grade6EventYn = null,
                            schoolCourseType = "고등학교",
                            dayNightType = "주간",
                            loadDateTime = null,
                        )

                    val apiResponse =
                        NeisScheduleApiResponse(
                            schoolSchedule =
                                listOf(
                                    SchoolScheduleWrapper(
                                        head = null,
                                        row = listOf(scheduleInfo1, scheduleInfo2),
                                    ),
                                ),
                        )

                    beforeEach {
                        every {
                            mockNeisApiClient.getSchoolSchedule(
                                key = "test-api-key",
                                atptOfcdcScCode = "F10",
                                sdSchulCode = "7380292",
                                aa_ymd = null,
                                aa_from_ymd = "20251216",
                                aa_to_ymd = "20251217",
                            )
                        } returns apiResponse

                        every { mockScheduleRepository.deleteAll() } returns Unit
                        every { mockScheduleRepository.saveAll(any<List<ScheduleRedisEntity>>()) } returns listOf()
                    }

                    it("NEIS API를 호출하고 데이터를 Redis에 저장해야 한다") {
                        syncScheduleService.execute(fromDate, toDate)

                        verify(exactly = 1) {
                            mockNeisApiClient.getSchoolSchedule(
                                key = "test-api-key",
                                atptOfcdcScCode = "F10",
                                sdSchulCode = "7380292",
                                aa_ymd = null,
                                aa_from_ymd = "20251216",
                                aa_to_ymd = "20251217",
                            )
                        }

                        val savedEntitiesSlot = slot<List<ScheduleRedisEntity>>()
                        verify(exactly = 1) { mockScheduleRepository.saveAll(capture(savedEntitiesSlot)) }

                        val savedEntities = savedEntitiesSlot.captured
                        savedEntities.size shouldBe 2
                        savedEntities[0].schoolCode shouldBe "7380292"
                        savedEntities[0].date shouldBe LocalDate.of(2025, 12, 16)
                        savedEntities[0].targetGrades shouldBe listOf(1, 2, 3)
                        savedEntities[1].date shouldBe LocalDate.of(2025, 12, 17)
                    }
                }

                context("NEIS API 응답이 비어있을 때") {
                    val fromDate = LocalDate.of(2025, 12, 16)
                    val toDate = LocalDate.of(2025, 12, 17)

                    val apiResponse =
                        NeisScheduleApiResponse(
                            schoolSchedule = null,
                        )

                    beforeEach {
                        every { mockNeisEnvironment.key } returns "test-api-key"
                        every { mockNeisEnvironment.officeCode } returns "F10"
                        every { mockNeisEnvironment.schoolCode } returns "7380292"

                        every {
                            mockNeisApiClient.getSchoolSchedule(
                                key = any(),
                                atptOfcdcScCode = any(),
                                sdSchulCode = any(),
                                aa_ymd = any(),
                                aa_from_ymd = any(),
                                aa_to_ymd = any(),
                            )
                        } returns apiResponse

                        every { mockScheduleRepository.deleteAll() } returns Unit
                        every { mockScheduleRepository.saveAll(any<List<ScheduleRedisEntity>>()) } returns listOf()
                    }

                    it("빈 목록을 저장해야 한다") {
                        syncScheduleService.execute(fromDate, toDate)

                        val savedEntitiesSlot = slot<List<ScheduleRedisEntity>>()
                        verify(exactly = 1) { mockScheduleRepository.saveAll(capture(savedEntitiesSlot)) }

                        savedEntitiesSlot.captured.size shouldBe 0
                    }
                }

                context("NEIS API 응답의 row가 null일 때") {
                    val fromDate = LocalDate.of(2025, 12, 16)
                    val toDate = LocalDate.of(2025, 12, 17)

                    val apiResponse =
                        NeisScheduleApiResponse(
                            schoolSchedule =
                                listOf(
                                    SchoolScheduleWrapper(
                                        head = null,
                                        row = null,
                                    ),
                                ),
                        )

                    beforeEach {
                        every { mockNeisEnvironment.key } returns "test-api-key"
                        every { mockNeisEnvironment.officeCode } returns "F10"
                        every { mockNeisEnvironment.schoolCode } returns "7380292"

                        every {
                            mockNeisApiClient.getSchoolSchedule(
                                key = any(),
                                atptOfcdcScCode = any(),
                                sdSchulCode = any(),
                                aa_ymd = any(),
                                aa_from_ymd = any(),
                                aa_to_ymd = any(),
                            )
                        } returns apiResponse

                        every { mockScheduleRepository.deleteAll() } returns Unit
                        every { mockScheduleRepository.saveAll(any<List<ScheduleRedisEntity>>()) } returns listOf()
                    }

                    it("빈 목록을 저장해야 한다") {
                        syncScheduleService.execute(fromDate, toDate)

                        val savedEntitiesSlot = slot<List<ScheduleRedisEntity>>()
                        verify(exactly = 1) { mockScheduleRepository.saveAll(capture(savedEntitiesSlot)) }

                        savedEntitiesSlot.captured.size shouldBe 0
                    }
                }
            }
        }
    })
