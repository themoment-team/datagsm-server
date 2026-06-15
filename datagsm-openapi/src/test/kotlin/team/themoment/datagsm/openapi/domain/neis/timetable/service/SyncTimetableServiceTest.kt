package team.themoment.datagsm.openapi.domain.neis.timetable.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import team.themoment.datagsm.common.domain.neis.dto.internal.HisTimetableWrapper
import team.themoment.datagsm.common.domain.neis.dto.internal.NeisTimetableApiResponse
import team.themoment.datagsm.common.domain.neis.dto.internal.TimetableInfo
import team.themoment.datagsm.common.domain.neis.timetable.entity.TimetableRedisEntity
import team.themoment.datagsm.common.domain.neis.timetable.repository.TimetableRedisRepository
import team.themoment.datagsm.common.global.data.NeisEnvironment
import team.themoment.datagsm.openapi.domain.neis.timetable.service.impl.SyncTimetableServiceImpl
import team.themoment.datagsm.openapi.global.thirdparty.feign.neis.NeisApiClient
import java.time.LocalDate

class SyncTimetableServiceTest :
    DescribeSpec({

        val mockNeisApiClient = mockk<NeisApiClient>()
        val mockTimetableRepository = mockk<TimetableRedisRepository>()
        val mockNeisEnvironment =
            mockk<NeisEnvironment>().apply {
                every { key } returns "test-api-key"
                every { officeCode } returns "F10"
                every { schoolCode } returns "7380292"
            }

        val syncTimetableService =
            SyncTimetableServiceImpl(
                mockNeisApiClient,
                mockTimetableRepository,
                mockNeisEnvironment,
            )

        afterEach {
            clearAllMocks()
        }

        describe("SyncTimetableService 클래스의") {
            describe("execute 메서드는") {

                context("NEIS API로부터 정상적으로 데이터를 받아올 때") {
                    val fromDate = LocalDate.of(2025, 3, 1)
                    val toDate = LocalDate.of(2026, 2, 28)

                    val timetableInfo1 =
                        TimetableInfo(
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            academicYear = "2025",
                            semester = "1",
                            timetableDate = "20250401",
                            grade = "1",
                            classNum = "1",
                            period = "1",
                            subject = "국어",
                            loadDateTime = null,
                        )
                    val timetableInfo2 =
                        TimetableInfo(
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            academicYear = "2025",
                            semester = "1",
                            timetableDate = "20250401",
                            grade = "2",
                            classNum = "3",
                            period = "2",
                            subject = "수학",
                            loadDateTime = null,
                        )

                    val apiResponse =
                        NeisTimetableApiResponse(
                            hisTimetable =
                                listOf(
                                    HisTimetableWrapper(
                                        head = null,
                                        row = listOf(timetableInfo1, timetableInfo2),
                                    ),
                                ),
                        )

                    beforeEach {
                        every {
                            mockNeisApiClient.getHisTimetable(
                                key = "test-api-key",
                                pIndex = any(),
                                pSize = any(),
                                atptOfcdcScCode = "F10",
                                sdSchulCode = "7380292",
                                tiFromYmd = "20250301",
                                tiToYmd = "20260228",
                            )
                        } returns apiResponse

                        every { mockTimetableRepository.saveAll(any<Iterable<TimetableRedisEntity>>()) } answers {
                            @Suppress("UNCHECKED_CAST")
                            (firstArg() as Iterable<TimetableRedisEntity>).toList()
                        }
                        every { mockTimetableRepository.deleteAll() } returns Unit
                    }

                    it("NEIS API를 호출하고 데이터를 Redis에 저장해야 한다") {
                        syncTimetableService.execute(fromDate, toDate)

                        verify(atLeast = 1) {
                            mockNeisApiClient.getHisTimetable(
                                key = "test-api-key",
                                pIndex = any(),
                                pSize = any(),
                                atptOfcdcScCode = "F10",
                                sdSchulCode = "7380292",
                                tiFromYmd = "20250301",
                                tiToYmd = "20260228",
                            )
                        }

                        verify(exactly = 1) { mockTimetableRepository.deleteAll() }
                        verify(exactly = 1) { mockTimetableRepository.saveAll(any<Iterable<TimetableRedisEntity>>()) }
                    }
                }

                context("일부 교시가 공강이라 NEIS 응답에서 누락되었을 때") {
                    val fromDate = LocalDate.of(2025, 3, 1)
                    val toDate = LocalDate.of(2026, 2, 28)

                    val timetableInfo1 =
                        TimetableInfo(
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            academicYear = "2025",
                            semester = "1",
                            timetableDate = "20250401",
                            grade = "1",
                            classNum = "1",
                            period = "1",
                            subject = "국어",
                            loadDateTime = null,
                        )
                    val timetableInfo5 =
                        TimetableInfo(
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            academicYear = "2025",
                            semester = "1",
                            timetableDate = "20250401",
                            grade = "1",
                            classNum = "1",
                            period = "5",
                            subject = "영어",
                            loadDateTime = null,
                        )

                    val apiResponse =
                        NeisTimetableApiResponse(
                            hisTimetable =
                                listOf(
                                    HisTimetableWrapper(
                                        head = null,
                                        row = listOf(timetableInfo1, timetableInfo5),
                                    ),
                                ),
                        )

                    val savedEntitiesSlot = slot<Iterable<TimetableRedisEntity>>()

                    beforeEach {
                        every { mockNeisEnvironment.key } returns "test-api-key"
                        every { mockNeisEnvironment.officeCode } returns "F10"
                        every { mockNeisEnvironment.schoolCode } returns "7380292"

                        every {
                            mockNeisApiClient.getHisTimetable(
                                key = any(),
                                pIndex = any(),
                                pSize = any(),
                                atptOfcdcScCode = any(),
                                sdSchulCode = any(),
                                tiFromYmd = any(),
                                tiToYmd = any(),
                            )
                        } returns apiResponse

                        every { mockTimetableRepository.saveAll(capture(savedEntitiesSlot)) } answers {
                            @Suppress("UNCHECKED_CAST")
                            (firstArg() as Iterable<TimetableRedisEntity>).toList()
                        }
                        every { mockTimetableRepository.deleteAll() } returns Unit
                    }

                    it("1~7교시 슬롯을 모두 채우고 공강 교시는 공강으로 저장해야 한다") {
                        syncTimetableService.execute(fromDate, toDate)

                        val savedEntities = savedEntitiesSlot.captured.toList()

                        savedEntities shouldHaveSize 7
                        savedEntities.map { it.period } shouldContainExactlyInAnyOrder (1..7).toList()
                        savedEntities.first { it.period == 1 }.subject shouldBe "국어"
                        savedEntities.first { it.period == 5 }.subject shouldBe "영어"
                        savedEntities
                            .filter { it.period in listOf(2, 3, 4, 6, 7) }
                            .forEach { it.subject shouldBe "공강" }
                    }
                }

                context("NEIS API 응답이 비어있을 때") {
                    val fromDate = LocalDate.of(2025, 3, 1)
                    val toDate = LocalDate.of(2026, 2, 28)

                    val apiResponse =
                        NeisTimetableApiResponse(
                            hisTimetable = null,
                        )

                    beforeEach {
                        every { mockNeisEnvironment.key } returns "test-api-key"
                        every { mockNeisEnvironment.officeCode } returns "F10"
                        every { mockNeisEnvironment.schoolCode } returns "7380292"

                        every {
                            mockNeisApiClient.getHisTimetable(
                                key = any(),
                                pIndex = any(),
                                pSize = any(),
                                atptOfcdcScCode = any(),
                                sdSchulCode = any(),
                                tiFromYmd = any(),
                                tiToYmd = any(),
                            )
                        } returns apiResponse

                        every { mockTimetableRepository.saveAll(any<Iterable<TimetableRedisEntity>>()) } answers {
                            @Suppress("UNCHECKED_CAST")
                            (firstArg() as Iterable<TimetableRedisEntity>).toList()
                        }
                        every { mockTimetableRepository.deleteAll() } returns Unit
                    }

                    it("기존 데이터를 유지해야 한다") {
                        syncTimetableService.execute(fromDate, toDate)

                        verify(exactly = 0) { mockTimetableRepository.deleteAll() }
                        verify(exactly = 0) { mockTimetableRepository.saveAll(any<Iterable<TimetableRedisEntity>>()) }
                    }
                }

                context("NEIS API 응답의 row가 null일 때") {
                    val fromDate = LocalDate.of(2025, 3, 1)
                    val toDate = LocalDate.of(2026, 2, 28)

                    val apiResponse =
                        NeisTimetableApiResponse(
                            hisTimetable =
                                listOf(
                                    HisTimetableWrapper(
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
                            mockNeisApiClient.getHisTimetable(
                                key = any(),
                                pIndex = any(),
                                pSize = any(),
                                atptOfcdcScCode = any(),
                                sdSchulCode = any(),
                                tiFromYmd = any(),
                                tiToYmd = any(),
                            )
                        } returns apiResponse

                        every { mockTimetableRepository.saveAll(any<Iterable<TimetableRedisEntity>>()) } answers {
                            @Suppress("UNCHECKED_CAST")
                            (firstArg() as Iterable<TimetableRedisEntity>).toList()
                        }
                        every { mockTimetableRepository.deleteAll() } returns Unit
                    }

                    it("기존 데이터를 유지해야 한다") {
                        syncTimetableService.execute(fromDate, toDate)

                        verify(exactly = 0) { mockTimetableRepository.deleteAll() }
                        verify(exactly = 0) { mockTimetableRepository.saveAll(any<Iterable<TimetableRedisEntity>>()) }
                    }
                }
            }
        }
    })
