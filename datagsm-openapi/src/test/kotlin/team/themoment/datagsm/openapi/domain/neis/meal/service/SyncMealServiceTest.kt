package team.themoment.datagsm.openapi.domain.neis.meal.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import team.themoment.datagsm.common.domain.neis.dto.internal.MealServiceDietInfo
import team.themoment.datagsm.common.domain.neis.dto.internal.MealServiceDietInfoWrapper
import team.themoment.datagsm.common.domain.neis.dto.internal.NeisMealApiResponse
import team.themoment.datagsm.common.domain.neis.meal.entity.MealRedisEntity
import team.themoment.datagsm.common.domain.neis.meal.repository.MealRedisRepository
import team.themoment.datagsm.common.global.data.NeisEnvironment
import team.themoment.datagsm.openapi.domain.neis.meal.service.impl.SyncMealServiceImpl
import team.themoment.datagsm.openapi.global.thirdparty.feign.neis.NeisApiClient
import java.time.LocalDate

class SyncMealServiceTest :
    DescribeSpec({

        val mockNeisApiClient = mockk<NeisApiClient>()
        val mockMealRepository = mockk<MealRedisRepository>()
        val mockNeisEnvironment =
            mockk<NeisEnvironment>().apply {
                every { key } returns "test-api-key"
                every { officeCode } returns "F10"
                every { schoolCode } returns "7380292"
            }

        val syncMealService =
            SyncMealServiceImpl(
                mockNeisApiClient,
                mockMealRepository,
                mockNeisEnvironment,
            )

        afterEach {
            clearAllMocks()
        }

        describe("SyncMealService 클래스의") {
            describe("execute 메서드는") {

                context("NEIS API로부터 정상적으로 데이터를 받아올 때") {
                    val fromDate = LocalDate.of(2025, 12, 16)
                    val toDate = LocalDate.of(2025, 12, 17)

                    val mealInfo1 =
                        MealServiceDietInfo(
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            mealTypeCode = "1",
                            mealTypeName = "조식",
                            mealDate = "20251216",
                            mealServeCount = null,
                            dishName = "쌀밥<br/>김치찌개<br/>계란말이",
                            originInfo = "쌀:국내산",
                            calorieInfo = "800 Kcal",
                            nutritionInfo = "탄수화물:100g",
                            mealFromDate = null,
                            mealToDate = null,
                        )
                    val mealInfo2 =
                        MealServiceDietInfo(
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            mealTypeCode = "2",
                            mealTypeName = "중식",
                            mealDate = "20251217",
                            mealServeCount = null,
                            dishName = "쌀밥<br/>된장찌개<br/>불고기",
                            originInfo = "쌀:국내산",
                            calorieInfo = "900 Kcal",
                            nutritionInfo = "탄수화물:120g",
                            mealFromDate = null,
                            mealToDate = null,
                        )

                    val apiResponse =
                        NeisMealApiResponse(
                            mealServiceDietInfo =
                                listOf(
                                    MealServiceDietInfoWrapper(
                                        head = null,
                                        row = listOf(mealInfo1, mealInfo2),
                                    ),
                                ),
                        )

                    beforeEach {
                        every {
                            mockNeisApiClient.getMealServiceDietInfo(
                                key = "test-api-key",
                                pIndex = any(),
                                pSize = any(),
                                atptOfcdcScCode = "F10",
                                sdSchulCode = "7380292",
                                mlsvYmd = null,
                                mlsvFromYmd = "20251216",
                                mlsvToYmd = "20251217",
                            )
                        } returns apiResponse

                        every { mockMealRepository.saveAll(any<Iterable<MealRedisEntity>>()) } answers {
                            @Suppress("UNCHECKED_CAST")
                            (firstArg() as Iterable<MealRedisEntity>).toList()
                        }
                        every { mockMealRepository.findAllIds() } returns emptySet()
                        every { mockMealRepository.deleteAllById(any()) } returns Unit
                    }

                    it("NEIS API를 호출하고 데이터를 Redis에 저장해야 한다") {
                        syncMealService.execute(fromDate, toDate)

                        verify(atLeast = 1) {
                            mockNeisApiClient.getMealServiceDietInfo(
                                key = "test-api-key",
                                pIndex = any(),
                                pSize = any(),
                                atptOfcdcScCode = "F10",
                                sdSchulCode = "7380292",
                                mlsvYmd = null,
                                mlsvFromYmd = "20251216",
                                mlsvToYmd = "20251217",
                            )
                        }

                        verify(exactly = 0) { mockMealRepository.deleteAll() }
                        verify(exactly = 1) { mockMealRepository.deleteAllById(any()) }
                        verify(exactly = 1) { mockMealRepository.saveAll(any<Iterable<MealRedisEntity>>()) }
                    }
                }

                context("NEIS API 응답이 비어있을 때") {
                    val fromDate = LocalDate.of(2025, 12, 16)
                    val toDate = LocalDate.of(2025, 12, 17)

                    val apiResponse =
                        NeisMealApiResponse(
                            mealServiceDietInfo = null,
                        )

                    beforeEach {
                        every { mockNeisEnvironment.key } returns "test-api-key"
                        every { mockNeisEnvironment.officeCode } returns "F10"
                        every { mockNeisEnvironment.schoolCode } returns "7380292"

                        every {
                            mockNeisApiClient.getMealServiceDietInfo(
                                key = any(),
                                pIndex = any(),
                                pSize = any(),
                                atptOfcdcScCode = any(),
                                sdSchulCode = any(),
                                mlsvYmd = any(),
                                mlsvFromYmd = any(),
                                mlsvToYmd = any(),
                            )
                        } returns apiResponse

                        every { mockMealRepository.saveAll(any<Iterable<MealRedisEntity>>()) } answers {
                            @Suppress("UNCHECKED_CAST")
                            (firstArg() as Iterable<MealRedisEntity>).toList()
                        }
                        every { mockMealRepository.findAllIds() } returns emptySet()
                        every { mockMealRepository.deleteAllById(any()) } returns Unit
                    }

                    it("기존 데이터를 유지해야 한다") {
                        syncMealService.execute(fromDate, toDate)

                        verify(exactly = 0) { mockMealRepository.deleteAll() }
                        verify(exactly = 0) { mockMealRepository.deleteAllById(any()) }
                        verify(exactly = 0) { mockMealRepository.saveAll(any<Iterable<MealRedisEntity>>()) }
                    }
                }

                context("기존에 저장된 급식 중 이번에 받지 않은 항목이 있을 때") {
                    val fromDate = LocalDate.of(2025, 12, 16)
                    val toDate = LocalDate.of(2025, 12, 17)
                    val savedIds = slot<Iterable<MealRedisEntity>>()
                    val deletedIds = slot<Iterable<String>>()

                    beforeEach {
                        every { mockNeisEnvironment.key } returns "test-api-key"
                        every { mockNeisEnvironment.officeCode } returns "F10"
                        every { mockNeisEnvironment.schoolCode } returns "7380292"
                        stubMealPages(mockNeisApiClient, listOf(listOf(mealRow("20251216", "2"))))
                        every { mockMealRepository.findAllIds() } returns
                            setOf("7380292_20251216_2", "7380292_20240101_2")
                        every { mockMealRepository.saveAll(capture(savedIds)) } answers { savedIds.captured.toList() }
                        every { mockMealRepository.deleteAllById(capture(deletedIds)) } returns Unit
                    }

                    it("전체를 지우지 않고 덮어쓴 뒤 받지 않은 항목만 삭제해야 한다") {
                        syncMealService.execute(fromDate, toDate)

                        verify(exactly = 0) { mockMealRepository.deleteAll() }
                        savedIds.captured.map { it.id } shouldBe listOf("7380292_20251216_2")
                        deletedIds.captured.toSet() shouldBe setOf("7380292_20240101_2")
                        verifyOrder {
                            mockMealRepository.saveAll(any<Iterable<MealRedisEntity>>())
                            mockMealRepository.deleteAllById(any())
                        }
                    }
                }

                context("알 수 없는 급식 유형 코드가 섞여 있을 때") {
                    val fromDate = LocalDate.of(2025, 12, 16)
                    val toDate = LocalDate.of(2025, 12, 17)
                    val savedIds = slot<Iterable<MealRedisEntity>>()

                    beforeEach {
                        every { mockNeisEnvironment.key } returns "test-api-key"
                        every { mockNeisEnvironment.officeCode } returns "F10"
                        every { mockNeisEnvironment.schoolCode } returns "7380292"
                        stubMealPages(
                            mockNeisApiClient,
                            listOf(listOf(mealRow("20251216", "2"), mealRow("20251216", "9"), mealRow("20251217", "3"))),
                        )
                        every { mockMealRepository.findAllIds() } returns emptySet()
                        every { mockMealRepository.saveAll(capture(savedIds)) } answers { savedIds.captured.toList() }
                        every { mockMealRepository.deleteAllById(any()) } returns Unit
                    }

                    it("그 행만 건너뛰고 나머지를 저장해야 한다") {
                        syncMealService.execute(fromDate, toDate)

                        savedIds.captured.map { it.id } shouldBe listOf("7380292_20251216_2", "7380292_20251217_3")
                    }
                }

                context("기존에 저장된 급식 행이 이번에 형식 오류로 건너뛰어졌을 때") {
                    val fromDate = LocalDate.of(2025, 12, 16)
                    val toDate = LocalDate.of(2025, 12, 17)

                    beforeEach {
                        every { mockNeisEnvironment.key } returns "test-api-key"
                        every { mockNeisEnvironment.officeCode } returns "F10"
                        every { mockNeisEnvironment.schoolCode } returns "7380292"
                        // 석식 행의 유형 코드가 깨져 id를 만들 수 없으므로, 기존 석식은 누락분과 구분되지 않는다.
                        stubMealPages(mockNeisApiClient, listOf(listOf(mealRow("20251216", "2"), mealRow("20251216", "X"))))
                        every { mockMealRepository.findAllIds() } returns setOf("7380292_20251216_2", "7380292_20251216_3")
                        every { mockMealRepository.saveAll(any<Iterable<MealRedisEntity>>()) } answers
                            { firstArg<Iterable<MealRedisEntity>>().toList() }
                        every { mockMealRepository.deleteAllById(any()) } returns Unit
                    }

                    it("정상 행은 저장하되 기존 데이터는 지우지 않아야 한다") {
                        syncMealService.execute(fromDate, toDate)

                        verify(exactly = 1) { mockMealRepository.saveAll(any<Iterable<MealRedisEntity>>()) }
                        verify(exactly = 0) { mockMealRepository.deleteAllById(any()) }
                    }
                }

                context("꽉 찬 페이지에서 일부 행을 건너뛰었을 때") {
                    val fromDate = LocalDate.of(2025, 12, 16)
                    val toDate = LocalDate.of(2025, 12, 17)
                    val savedIds = slot<Iterable<MealRedisEntity>>()
                    val fullPage = List(PAGE_SIZE - 1) { mealRow("20251216", "2") } + mealRow("20251216", "9")

                    beforeEach {
                        every { mockNeisEnvironment.key } returns "test-api-key"
                        every { mockNeisEnvironment.officeCode } returns "F10"
                        every { mockNeisEnvironment.schoolCode } returns "7380292"
                        stubMealPages(mockNeisApiClient, listOf(fullPage, listOf(mealRow("20251217", "3"))))
                        every { mockMealRepository.findAllIds() } returns emptySet()
                        every { mockMealRepository.saveAll(capture(savedIds)) } answers { savedIds.captured.toList() }
                        every { mockMealRepository.deleteAllById(any()) } returns Unit
                    }

                    it("다음 페이지까지 받아와야 한다") {
                        syncMealService.execute(fromDate, toDate)

                        savedIds.captured.map { it.id } shouldContain "7380292_20251217_3"
                        // 서비스의 페이지 크기가 PAGE_SIZE와 달라지면 2페이지를 요청하지 않아 여기서 실패한다.
                        verify(exactly = 1) {
                            mockNeisApiClient.getMealServiceDietInfo(
                                key = any(),
                                pIndex = 2,
                                pSize = PAGE_SIZE,
                                atptOfcdcScCode = any(),
                                sdSchulCode = any(),
                                mlsvYmd = any(),
                                mlsvFromYmd = any(),
                                mlsvToYmd = any(),
                            )
                        }
                    }
                }
            }
        }
    })

private const val PAGE_SIZE = 1000

private fun mealRow(
    mealDate: String,
    mealTypeCode: String,
) = MealServiceDietInfo(
    officeCode = "F10",
    officeName = "광주광역시교육청",
    schoolCode = "7380292",
    schoolName = "광주소프트웨어마이스터고등학교",
    mealTypeCode = mealTypeCode,
    mealTypeName = "중식",
    mealDate = mealDate,
    mealServeCount = null,
    dishName = "쌀밥",
    originInfo = null,
    calorieInfo = null,
    nutritionInfo = null,
    mealFromDate = null,
    mealToDate = null,
)

private fun stubMealPages(
    neisApiClient: NeisApiClient,
    pages: List<List<MealServiceDietInfo>>,
) {
    pages.forEachIndexed { index, rows ->
        every {
            neisApiClient.getMealServiceDietInfo(
                key = any(),
                pIndex = index + 1,
                pSize = any(),
                atptOfcdcScCode = any(),
                sdSchulCode = any(),
                mlsvYmd = any(),
                mlsvFromYmd = any(),
                mlsvToYmd = any(),
            )
        } returns NeisMealApiResponse(mealServiceDietInfo = listOf(MealServiceDietInfoWrapper(head = null, row = rows)))
    }
}
