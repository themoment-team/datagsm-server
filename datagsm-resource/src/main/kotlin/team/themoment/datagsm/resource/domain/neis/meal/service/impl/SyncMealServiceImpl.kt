package team.themoment.datagsm.resource.domain.neis.meal.service.impl

import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.neis.MealRedisEntity
import team.themoment.datagsm.common.domain.neis.MealType
import team.themoment.datagsm.common.domain.neis.meal.repository.MealRedisRepository
import team.themoment.datagsm.resource.domain.neis.common.data.NeisEnvironment
import team.themoment.datagsm.resource.domain.neis.meal.service.SyncMealService
import team.themoment.datagsm.resource.global.thirdparty.feign.neis.NeisApiClient
import team.themoment.datagsm.resource.global.thirdparty.feign.neis.dto.MealServiceDietInfo
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class SyncMealServiceImpl(
    private val neisApiClient: NeisApiClient,
    private val mealRedisRepository: MealRedisRepository,
    private val neisEnvironment: NeisEnvironment,
) : SyncMealService {
    @Retryable(
        maxAttempts = 3,
        backoff = Backoff(delay = 5000, multiplier = 2.0),
    )
    override fun execute(
        fromDate: LocalDate,
        toDate: LocalDate,
    ) {
        val mlsvFromYmd = fromDate.format(DATE_FORMATTER)
        val mlsvToYmd = toDate.format(DATE_FORMATTER)

        val allMealEntities = mutableListOf<MealRedisEntity>()
        var pageIndex = 1
        val pageSize = 1000

        do {
            val apiResponse =
                neisApiClient.getMealServiceDietInfo(
                    key = neisEnvironment.key,
                    pIndex = pageIndex,
                    pSize = pageSize,
                    atptOfcdcScCode = neisEnvironment.officeCode,
                    sdSchulCode = neisEnvironment.schoolCode,
                    mlsvYmd = null,
                    mlsvFromYmd = mlsvFromYmd,
                    mlsvToYmd = mlsvToYmd,
                )

            val meals =
                apiResponse.mealServiceDietInfo
                    ?.find { it.row != null }
                    ?.row
                    ?.map { convertToEntity(it) }
                    ?: emptyList()

            allMealEntities.addAll(meals)
            pageIndex++
        } while (meals.size == pageSize)

        if (allMealEntities.isNotEmpty()) {
            mealRedisRepository.deleteAll()
            mealRedisRepository.saveAll(allMealEntities)
        }
    }

    private fun convertToEntity(dto: MealServiceDietInfo): MealRedisEntity {
        val mealDate = LocalDate.parse(dto.mealDate, DATE_FORMATTER)
        val mealType = convertMealType(dto.mealTypeCode)
        val mealId = "${dto.schoolCode}_${dto.mealDate}_${dto.mealTypeCode}"

        val menuList =
            dto.dishName
                ?.replace("<br/>", "\n")
                ?.split("\n")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?: emptyList()

        val allergyInfo = extractAllergyInfo(dto.dishName)
        val serveCount = dto.mealServeCount?.toIntOrNull()

        return MealRedisEntity(
            id = mealId,
            schoolCode = dto.schoolCode,
            schoolName = dto.schoolName,
            officeCode = dto.officeCode,
            officeName = dto.officeName,
            date = mealDate,
            type = mealType,
            menu = menuList,
            allergyInfo = allergyInfo,
            calories = dto.calorieInfo,
            originInfo = dto.originInfo?.replace("<br/>", "\n"),
            nutritionInfo = dto.nutritionInfo?.replace("<br/>", "\n"),
            serveCount = serveCount,
        )
    }

    private fun convertMealType(mealTypeCode: String): MealType =
        when (mealTypeCode) {
            "1" -> {
                MealType.BREAKFAST
            }

            "2" -> {
                MealType.LUNCH
            }

            "3" -> {
                MealType.DINNER
            }

            else -> {
                throw IllegalArgumentException(
                    "Unknown meal type code: $mealTypeCode. Expected values are 1 (BREAKFAST), 2 (LUNCH), or 3 (DINNER)",
                )
            }
        }

    private fun extractAllergyInfo(dishName: String?): List<String> {
        if (dishName == null) return emptyList()

        val allergyRegex = "\\((\\d+\\.)+\\)".toRegex()
        val allergyMatches = allergyRegex.findAll(dishName)

        return allergyMatches
            .flatMap { match ->
                match.value
                    .removeSurrounding("(", ")")
                    .split(".")
                    .filter { it.isNotEmpty() }
            }.distinct()
            .toList()
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
    }
}
