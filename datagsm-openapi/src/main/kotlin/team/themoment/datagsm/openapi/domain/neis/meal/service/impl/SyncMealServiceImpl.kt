package team.themoment.datagsm.openapi.domain.neis.meal.service.impl

import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.neis.dto.internal.MealServiceDietInfo
import team.themoment.datagsm.common.domain.neis.meal.entity.MealRedisEntity
import team.themoment.datagsm.common.domain.neis.meal.entity.constant.MealType
import team.themoment.datagsm.common.domain.neis.meal.repository.MealRedisRepository
import team.themoment.datagsm.common.global.data.NeisEnvironment
import team.themoment.datagsm.openapi.domain.neis.meal.service.SyncMealService
import team.themoment.datagsm.openapi.global.thirdparty.feign.neis.NeisApiClient
import team.themoment.sdk.logging.logger.logger
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
        var skippedRowCount = 0

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

            val rows =
                apiResponse.mealServiceDietInfo
                    ?.find { it.row != null }
                    ?.row
                    ?: emptyList()

            val convertedEntities = rows.mapNotNull { convertOrSkip(it) }
            allMealEntities.addAll(convertedEntities)
            skippedRowCount += rows.size - convertedEntities.size
            pageIndex++
            // 건너뛴 행이 있어도 다음 페이지를 받아야 하므로 변환 전 행 수로 판단한다.
        } while (rows.size == pageSize)

        if (allMealEntities.isNotEmpty()) {
            // 전체를 지운 뒤 다시 넣으면 그 사이 조회가 빈 결과를 받으므로, 덮어쓴 뒤 이번에 받지 않은 항목만 지운다.
            val staleIds = mealRedisRepository.findAllIds() - allMealEntities.map { it.id }.toSet()
            mealRedisRepository.saveAll(allMealEntities)
            // 건너뛴 행은 id를 알 수 없어 그 행의 기존 데이터가 누락분으로 지워지므로, 건너뛴 행이 있으면 지우지 않는다.
            if (skippedRowCount == 0) {
                mealRedisRepository.deleteAllById(staleIds)
            } else {
                logger().warn("Kept {} stale meal entries because {} rows were skipped", staleIds.size, skippedRowCount)
            }
        }
    }

    // 형식이 어긋난 행 하나로 전체가 실패하면 재시도해도 결과가 같아, 그 행만 건너뛰고 나머지를 반영한다.
    // Error까지 잡으면 시스템 장애가 행 하나의 경고로 묻히므로 Exception만 건너뛰고, 원인 추적을 위해 예외를 함께 남긴다.
    private fun convertOrSkip(dto: MealServiceDietInfo): MealRedisEntity? =
        try {
            convertToEntity(dto)
        } catch (e: Exception) {
            logger().warn("Skipped invalid meal row for date {}", dto.mealDate, e)
            null
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
