package team.themoment.datagsm.openapi.domain.neis.schedule.service.impl

import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.neis.dto.internal.SchoolScheduleInfo
import team.themoment.datagsm.common.domain.neis.schedule.entity.ScheduleRedisEntity
import team.themoment.datagsm.common.domain.neis.schedule.repository.ScheduleRedisRepository
import team.themoment.datagsm.common.global.data.NeisEnvironment
import team.themoment.datagsm.openapi.domain.neis.schedule.service.SyncScheduleService
import team.themoment.datagsm.openapi.global.thirdparty.feign.neis.NeisApiClient
import team.themoment.sdk.logging.logger.logger
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class SyncScheduleServiceImpl(
    private val neisApiClient: NeisApiClient,
    private val scheduleRedisRepository: ScheduleRedisRepository,
    private val neisEnvironment: NeisEnvironment,
) : SyncScheduleService {
    @Retryable(
        maxAttempts = 3,
        backoff = Backoff(delay = 5000, multiplier = 2.0),
    )
    override fun execute(
        fromDate: LocalDate,
        toDate: LocalDate,
    ) {
        val aaFromYmd = fromDate.format(DATE_FORMATTER)
        val aaToYmd = toDate.format(DATE_FORMATTER)

        val allScheduleEntities = mutableListOf<ScheduleRedisEntity>()
        var pageIndex = 1
        val pageSize = 1000

        do {
            val apiResponse =
                neisApiClient.getSchoolSchedule(
                    key = neisEnvironment.key,
                    pIndex = pageIndex,
                    pSize = pageSize,
                    atptOfcdcScCode = neisEnvironment.officeCode,
                    sdSchulCode = neisEnvironment.schoolCode,
                    aa_ymd = null,
                    aa_from_ymd = aaFromYmd,
                    aa_to_ymd = aaToYmd,
                )

            val rows =
                apiResponse.schoolSchedule
                    ?.find { it.row != null }
                    ?.row
                    ?: emptyList()

            allScheduleEntities.addAll(rows.mapNotNull { convertOrSkip(it) })
            pageIndex++
            // 건너뛴 행이 있어도 다음 페이지를 받아야 하므로 변환 전 행 수로 판단한다.
        } while (rows.size == pageSize)

        if (allScheduleEntities.isNotEmpty()) {
            // 전체를 지운 뒤 다시 넣으면 그 사이 조회가 빈 결과를 받으므로, 덮어쓴 뒤 이번에 받지 않은 항목만 지운다.
            val staleIds = scheduleRedisRepository.findAllIds() - allScheduleEntities.map { it.id }.toSet()
            scheduleRedisRepository.saveAll(allScheduleEntities)
            scheduleRedisRepository.deleteAllById(staleIds)
        }
    }

    // 형식이 어긋난 행 하나로 전체가 실패하면 재시도해도 결과가 같아, 그 행만 건너뛰고 나머지를 반영한다.
    private fun convertOrSkip(dto: SchoolScheduleInfo): ScheduleRedisEntity? =
        runCatching { convertToEntity(dto) }
            .onFailure { logger().warn("Skipped invalid schedule row for date {} with reason {}", dto.scheduleDate, it.message) }
            .getOrNull()

    private fun convertToEntity(dto: SchoolScheduleInfo): ScheduleRedisEntity {
        val scheduleDate = LocalDate.parse(dto.scheduleDate, DATE_FORMATTER)
        val scheduleId = "${dto.schoolCode}_${dto.scheduleDate}"

        val targetGrades =
            parseTargetGrades(
                grade1Yn = dto.grade1EventYn,
                grade2Yn = dto.grade2EventYn,
                grade3Yn = dto.grade3EventYn,
                grade4Yn = dto.grade4EventYn,
                grade5Yn = dto.grade5EventYn,
                grade6Yn = dto.grade6EventYn,
            )

        return ScheduleRedisEntity(
            id = scheduleId,
            schoolCode = dto.schoolCode,
            schoolName = dto.schoolName,
            officeCode = dto.officeCode,
            officeName = dto.officeName,
            date = scheduleDate,
            academicYear = dto.academicYear,
            eventName = dto.eventName,
            eventContent = dto.eventContent,
            dayCategory = dto.dayCategory,
            schoolCourseType = dto.schoolCourseType,
            dayNightType = dto.dayNightType,
            targetGrades = targetGrades,
        )
    }

    private fun parseTargetGrades(
        grade1Yn: String?,
        grade2Yn: String?,
        grade3Yn: String?,
        grade4Yn: String?,
        grade5Yn: String?,
        grade6Yn: String?,
    ): List<Int> =
        listOf(
            1 to grade1Yn,
            2 to grade2Yn,
            3 to grade3Yn,
            4 to grade4Yn,
            5 to grade5Yn,
            6 to grade6Yn,
        ).filter { it.second == "Y" }
            .map { it.first }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
    }
}
