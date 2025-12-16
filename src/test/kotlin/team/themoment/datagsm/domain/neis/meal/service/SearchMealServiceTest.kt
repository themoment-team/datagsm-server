package team.themoment.datagsm.domain.neis.meal.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.domain.neis.meal.entity.MealRedisEntity
import team.themoment.datagsm.domain.neis.meal.entity.constant.MealType
import team.themoment.datagsm.domain.neis.meal.repository.MealRedisRepository
import team.themoment.datagsm.domain.neis.meal.service.impl.SearchMealServiceImpl
import java.time.LocalDate

class SearchMealServiceTest :
    DescribeSpec({

        val mockMealRepository = mockk<MealRedisRepository>()
        val searchMealService = SearchMealServiceImpl(mockMealRepository)

        afterEach {
            clearAllMocks()
        }

        describe("SearchMealService 클래스의") {
            describe("execute 메서드는") {

                context("특정 날짜로 검색할 때") {
                    val targetDate = LocalDate.of(2025, 12, 16)
                    val meal1 =
                        MealRedisEntity(
                            id = "7380292_20251216_1",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = targetDate,
                            type = MealType.BREAKFAST,
                            menu = listOf("쌀밥", "김치찌개", "계란말이"),
                            allergyInfo = null,
                            calories = "800 Kcal",
                            originInfo = "쌀:국내산",
                            nutritionInfo = "탄수화물:100g",
                            serveCount = null,
                        )
                    val meal2 =
                        MealRedisEntity(
                            id = "7380292_20251216_2",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = targetDate,
                            type = MealType.LUNCH,
                            menu = listOf("쌀밥", "된장찌개", "불고기"),
                            allergyInfo = listOf("1", "5", "6"),
                            calories = "900 Kcal",
                            originInfo = "쌀:국내산",
                            nutritionInfo = "탄수화물:120g",
                            serveCount = null,
                        )

                    beforeEach {
                        every { mockMealRepository.findAll() } returns listOf(meal1, meal2)
                    }

                    it("해당 날짜의 모든 급식 정보를 반환해야 한다") {
                        val result = searchMealService.execute(date = targetDate, fromDate = null, toDate = null)

                        result.size shouldBe 2
                        result[0].mealId shouldBe "7380292_20251216_1"
                        result[0].mealType shouldBe MealType.BREAKFAST
                        result[0].mealMenu shouldBe listOf("쌀밥", "김치찌개", "계란말이")
                        result[1].mealId shouldBe "7380292_20251216_2"
                        result[1].mealType shouldBe MealType.LUNCH

                        verify(exactly = 1) { mockMealRepository.findAll() }
                    }
                }

                context("날짜 범위로 검색할 때") {
                    val fromDate = LocalDate.of(2025, 12, 16)
                    val toDate = LocalDate.of(2025, 12, 17)
                    val meal1 =
                        MealRedisEntity(
                            id = "7380292_20251216_1",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = LocalDate.of(2025, 12, 16),
                            type = MealType.BREAKFAST,
                            menu = listOf("쌀밥"),
                            allergyInfo = null,
                            calories = "800 Kcal",
                            originInfo = "쌀:국내산",
                            nutritionInfo = "탄수화물:100g",
                            serveCount = null,
                        )
                    val meal2 =
                        MealRedisEntity(
                            id = "7380292_20251217_1",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = LocalDate.of(2025, 12, 17),
                            type = MealType.BREAKFAST,
                            menu = listOf("쌀밥"),
                            allergyInfo = null,
                            calories = "800 Kcal",
                            originInfo = "쌀:국내산",
                            nutritionInfo = "탄수화물:100g",
                            serveCount = null,
                        )
                    val meal3 =
                        MealRedisEntity(
                            id = "7380292_20251218_1",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = LocalDate.of(2025, 12, 18),
                            type = MealType.BREAKFAST,
                            menu = listOf("쌀밥"),
                            allergyInfo = null,
                            calories = "800 Kcal",
                            originInfo = "쌀:국내산",
                            nutritionInfo = "탄수화물:100g",
                            serveCount = null,
                        )

                    beforeEach {
                        every { mockMealRepository.findAll() } returns listOf(meal1, meal2, meal3)
                    }

                    it("날짜 범위 내의 급식 정보만 반환해야 한다") {
                        val result = searchMealService.execute(date = null, fromDate = fromDate, toDate = toDate)

                        result.size shouldBe 2
                        result[0].mealDate shouldBe LocalDate.of(2025, 12, 16)
                        result[1].mealDate shouldBe LocalDate.of(2025, 12, 17)

                        verify(exactly = 1) { mockMealRepository.findAll() }
                    }
                }

                context("검색 결과가 없을 때") {
                    beforeEach {
                        every { mockMealRepository.findAll() } returns emptyList()
                    }

                    it("빈 목록을 반환해야 한다") {
                        val result = searchMealService.execute(date = LocalDate.now(), fromDate = null, toDate = null)

                        result.size shouldBe 0

                        verify(exactly = 1) { mockMealRepository.findAll() }
                    }
                }

                context("파라미터가 모두 null일 때") {
                    val meal1 =
                        MealRedisEntity(
                            id = "7380292_20251216_1",
                            schoolCode = "7380292",
                            schoolName = "광주소프트웨어마이스터고등학교",
                            officeCode = "F10",
                            officeName = "광주광역시교육청",
                            date = LocalDate.of(2025, 12, 16),
                            type = MealType.BREAKFAST,
                            menu = listOf("쌀밥"),
                            allergyInfo = null,
                            calories = "800 Kcal",
                            originInfo = "쌀:국내산",
                            nutritionInfo = "탄수화물:100g",
                            serveCount = null,
                        )

                    beforeEach {
                        every { mockMealRepository.findAll() } returns listOf(meal1)
                    }

                    it("모든 급식 정보를 반환해야 한다") {
                        val result = searchMealService.execute(date = null, fromDate = null, toDate = null)

                        result.size shouldBe 1
                        result[0].mealId shouldBe "7380292_20251216_1"

                        verify(exactly = 1) { mockMealRepository.findAll() }
                    }
                }
            }
        }
    })
