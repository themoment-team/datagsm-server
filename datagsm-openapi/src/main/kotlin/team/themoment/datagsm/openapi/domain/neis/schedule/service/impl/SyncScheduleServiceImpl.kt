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
import java.security.MessageDigest
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
        var skippedRowCount = 0

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

            val convertedEntities = rows.mapNotNull { convertOrSkip(it) }
            allScheduleEntities.addAll(convertedEntities)
            skippedRowCount += rows.size - convertedEntities.size
            pageIndex++
            // 건너뛴 행이 있어도 다음 페이지를 받아야 하므로 변환 전 행 수로 판단한다.
        } while (rows.size == pageSize)

        if (allScheduleEntities.isNotEmpty()) {
            // 전체를 지운 뒤 다시 넣으면 그 사이 조회가 빈 결과를 받으므로, 덮어쓴 뒤 이번에 받지 않은 항목만 지운다.
            val staleIds = scheduleRedisRepository.findAllIds() - allScheduleEntities.map { it.id }.toSet()
            scheduleRedisRepository.saveAll(allScheduleEntities)
            // 건너뛴 행은 id를 알 수 없어 그 행의 기존 데이터가 누락분으로 지워지므로, 건너뛴 행이 있으면 지우지 않는다.
            if (skippedRowCount == 0) {
                scheduleRedisRepository.deleteAllById(staleIds)
            } else {
                logger().warn("Kept {} stale schedule entries because {} rows were skipped", staleIds.size, skippedRowCount)
            }
        }
    }

    // 형식이 어긋난 행 하나로 전체가 실패하면 재시도해도 결과가 같아, 그 행만 건너뛰고 나머지를 반영한다.
    // Error까지 잡으면 시스템 장애가 행 하나의 경고로 묻히므로 Exception만 건너뛰고, 원인 추적을 위해 예외를 함께 남긴다.
    private fun convertOrSkip(dto: SchoolScheduleInfo): ScheduleRedisEntity? =
        try {
            convertToEntity(dto)
        } catch (e: Exception) {
            logger().warn("Skipped invalid schedule row for date {}", dto.scheduleDate, e)
            null
        }

    private fun convertToEntity(dto: SchoolScheduleInfo): ScheduleRedisEntity {
        val scheduleDate = LocalDate.parse(dto.scheduleDate, DATE_FORMATTER)
        val scheduleId = createScheduleId(dto)

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
            eventName = dto.eventName.trim(),
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

    // NEIS 학사일정에는 일정별 고유 ID가 없다. 날짜만으로 만들면 같은 날 일정이 서로 덮어쓰므로 행을 구분하는 값을 더하되,
    // 한글·공백이 섞인 긴 값이 API의 scheduleId로 나가지 않도록 해시로 줄인다. 내용이 같으면 동기화마다 같은 id가 된다.
    // 같은 행사명이라도 학년·내용·구분이 다른 행으로 올 수 있어 함께 넣고, 값끼리 이어 붙어 모호해지지 않게 구분자를 둔다.
    private fun createScheduleId(dto: SchoolScheduleInfo): String {
        val idSource =
            listOf(
                dto.eventName.trim(),
                dto.eventContent?.trim().orEmpty(),
                dto.dayCategory?.trim().orEmpty(),
                listOf(
                    dto.grade1EventYn,
                    dto.grade2EventYn,
                    dto.grade3EventYn,
                    dto.grade4EventYn,
                    dto.grade5EventYn,
                    dto.grade6EventYn,
                ).joinToString("") { it ?: "N" },
            ).joinToString(ID_SOURCE_SEPARATOR)
        val idSourceHash =
            MessageDigest
                .getInstance("SHA-256")
                .digest(idSource.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
                .take(SCHEDULE_ID_HASH_LENGTH)
        return "${dto.schoolCode}_${dto.scheduleDate}_$idSourceHash"
    }

    companion object {
        private const val SCHEDULE_ID_HASH_LENGTH = 16
        private const val ID_SOURCE_SEPARATOR = "\u001F"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
    }
}
