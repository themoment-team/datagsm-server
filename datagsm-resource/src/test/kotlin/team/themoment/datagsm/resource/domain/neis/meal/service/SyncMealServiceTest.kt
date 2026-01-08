package team.themoment.datagsm.resource.domain.neis.meal.service

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.common.domain.neis.MealRedisEntity
import team.themoment.datagsm.common.domain.neis.meal.repository.MealRedisRepository
import team.themoment.datagsm.resource.domain.neis.common.data.NeisEnvironment
import team.themoment.datagsm.resource.domain.neis.meal.service.impl.SyncMealServiceImpl
import team.themoment.datagsm.resource.global.thirdparty.feign.neis.NeisApiClient
import team.themoment.datagsm.resource.global.thirdparty.feign.neis.dto.MealServiceDietInfo
import team.themoment.datagsm.resource.global.thirdparty.feign.neis.dto.MealServiceDietInfoWrapper
import team.themoment.datagsm.resource.global.thirdparty.feign.neis.dto.NeisMealApiResponse
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
                        every { mockMealRepository.deleteAll() } returns Unit
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

                        verify(exactly = 1) { mockMealRepository.deleteAll() }
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
                        every { mockMealRepository.deleteAll() } returns Unit
                    }

                    it("기존 데이터를 유지해야 한다") {
                        syncMealService.execute(fromDate, toDate)

                        verify(exactly = 0) { mockMealRepository.deleteAll() }
                        verify(exactly = 0) { mockMealRepository.saveAll(any<Iterable<MealRedisEntity>>()) }
                    }
                }
            }
        }
    })
