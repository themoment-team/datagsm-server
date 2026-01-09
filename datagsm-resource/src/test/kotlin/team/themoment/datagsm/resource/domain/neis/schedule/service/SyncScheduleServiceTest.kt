package team.themoment.datagsm.resource.domain.neis.schedule.service

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.neis.schedule.entity.ScheduleRedisEntity
import team.themoment.datagsm.common.domain.neis.schedule.repository.ScheduleRedisRepository
import team.themoment.datagsm.common.domain.neis.dto.internal.NeisScheduleApiResponse
import team.themoment.datagsm.common.domain.neis.dto.internal.SchoolScheduleInfo
import team.themoment.datagsm.common.domain.neis.dto.internal.SchoolScheduleWrapper
import team.themoment.datagsm.common.global.data.NeisEnvironment
import team.themoment.datagsm.resource.domain.neis.schedule.service.impl.SyncScheduleServiceImpl
import team.themoment.datagsm.resource.global.thirdparty.feign.neis.NeisApiClient
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
                                pIndex = any(),
                                pSize = any(),
                                atptOfcdcScCode = "F10",
                                sdSchulCode = "7380292",
                                aa_ymd = null,
                                aa_from_ymd = "20251216",
                                aa_to_ymd = "20251217",
                            )
                        } returns apiResponse

                        every { mockScheduleRepository.saveAll(any<Iterable<ScheduleRedisEntity>>()) } answers {
                            @Suppress("UNCHECKED_CAST")
                            (firstArg() as Iterable<ScheduleRedisEntity>).toList()
                        }
                        every { mockScheduleRepository.deleteAll() } returns Unit
                    }

                    it("NEIS API를 호출하고 데이터를 Redis에 저장해야 한다") {
                        syncScheduleService.execute(fromDate, toDate)

                        verify(atLeast = 1) {
                            mockNeisApiClient.getSchoolSchedule(
                                key = "test-api-key",
                                pIndex = any(),
                                pSize = any(),
                                atptOfcdcScCode = "F10",
                                sdSchulCode = "7380292",
                                aa_ymd = null,
                                aa_from_ymd = "20251216",
                                aa_to_ymd = "20251217",
                            )
                        }

                        verify(exactly = 1) { mockScheduleRepository.deleteAll() }
                        verify(exactly = 1) { mockScheduleRepository.saveAll(any<Iterable<ScheduleRedisEntity>>()) }
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
                                pIndex = any(),
                                pSize = any(),
                                atptOfcdcScCode = any(),
                                sdSchulCode = any(),
                                aa_ymd = any(),
                                aa_from_ymd = any(),
                                aa_to_ymd = any(),
                            )
                        } returns apiResponse

                        every { mockScheduleRepository.saveAll(any<Iterable<ScheduleRedisEntity>>()) } answers {
                            @Suppress("UNCHECKED_CAST")
                            (firstArg() as Iterable<ScheduleRedisEntity>).toList()
                        }
                        every { mockScheduleRepository.deleteAll() } returns Unit
                    }

                    it("기존 데이터를 유지해야 한다") {
                        syncScheduleService.execute(fromDate, toDate)

                        verify(exactly = 0) { mockScheduleRepository.deleteAll() }
                        verify(exactly = 0) { mockScheduleRepository.saveAll(any<Iterable<ScheduleRedisEntity>>()) }
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
                                pIndex = any(),
                                pSize = any(),
                                atptOfcdcScCode = any(),
                                sdSchulCode = any(),
                                aa_ymd = any(),
                                aa_from_ymd = any(),
                                aa_to_ymd = any(),
                            )
                        } returns apiResponse

                        every { mockScheduleRepository.saveAll(any<Iterable<ScheduleRedisEntity>>()) } answers {
                            @Suppress("UNCHECKED_CAST")
                            (firstArg() as Iterable<ScheduleRedisEntity>).toList()
                        }
                        every { mockScheduleRepository.deleteAll() } returns Unit
                    }

                    it("기존 데이터를 유지해야 한다") {
                        syncScheduleService.execute(fromDate, toDate)

                        verify(exactly = 0) { mockScheduleRepository.deleteAll() }
                        verify(exactly = 0) { mockScheduleRepository.saveAll(any<Iterable<ScheduleRedisEntity>>()) }
                    }
                }
            }
        }
    })
