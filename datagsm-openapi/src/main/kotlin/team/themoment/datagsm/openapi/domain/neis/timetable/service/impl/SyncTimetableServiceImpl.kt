package team.themoment.datagsm.openapi.domain.neis.timetable.service.impl

import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.neis.dto.internal.TimetableInfo
import team.themoment.datagsm.common.domain.neis.timetable.entity.TimetableRedisEntity
import team.themoment.datagsm.common.domain.neis.timetable.repository.TimetableRedisRepository
import team.themoment.datagsm.common.global.data.NeisEnvironment
import team.themoment.datagsm.openapi.domain.neis.timetable.service.SyncTimetableService
import team.themoment.datagsm.openapi.global.thirdparty.feign.neis.NeisApiClient
import team.themoment.sdk.logging.logger.logger
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Service
class SyncTimetableServiceImpl(
    private val neisApiClient: NeisApiClient,
    private val timetableRedisRepository: TimetableRedisRepository,
    private val neisEnvironment: NeisEnvironment,
) : SyncTimetableService {
    @Retryable(
        maxAttempts = 3,
        backoff = Backoff(delay = 5000, multiplier = 2.0),
    )
    override fun execute(
        fromDate: LocalDate,
        toDate: LocalDate,
    ) {
        val tiFromYmd = fromDate.format(DATE_FORMATTER)
        val tiToYmd = toDate.format(DATE_FORMATTER)

        val allTimetableEntities = mutableListOf<TimetableRedisEntity>()

        var pageIndex = 1
        val pageSize = 1000
        val skippedRows = mutableListOf<TimetableInfo>()

        do {
            val apiResponse =
                neisApiClient.getHisTimetable(
                    key = neisEnvironment.key,
                    pIndex = pageIndex,
                    pSize = pageSize,
                    atptOfcdcScCode = neisEnvironment.officeCode,
                    sdSchulCode = neisEnvironment.schoolCode,
                    tiFromYmd = tiFromYmd,
                    tiToYmd = tiToYmd,
                )

            val rows =
                apiResponse.hisTimetable
                    ?.find { it.row != null }
                    ?.row
                    ?: emptyList()

            rows.forEach { row -> convertOrSkip(row)?.let { allTimetableEntities.add(it) } ?: skippedRows.add(row) }
            pageIndex++
            // 건너뛴 행이 있어도 다음 페이지를 받아야 하므로 변환 전 행 수로 판단한다.
        } while (rows.size == pageSize)

        if (allTimetableEntities.isNotEmpty()) {
            val filledTimetableEntities = fillEmptyPeriods(allTimetableEntities, skippedRows)
            // 전체를 지운 뒤 다시 넣으면 그 사이 조회가 빈 결과를 받으므로, 덮어쓴 뒤 이번에 받지 않은 항목만 지운다.
            val staleIds = timetableRedisRepository.findAllIds() - filledTimetableEntities.map { it.id }.toSet()
            timetableRedisRepository.saveAll(filledTimetableEntities)
            // 건너뛴 행은 id를 알 수 없어 그 행의 기존 데이터가 누락분으로 지워지므로, 건너뛴 행이 있으면 지우지 않는다.
            if (skippedRows.isEmpty()) {
                timetableRedisRepository.deleteAllById(staleIds)
            } else {
                logger().warn("Kept {} stale timetable entries because {} rows were skipped", staleIds.size, skippedRows.size)
            }
        }
    }

    // 건너뛴 행이 속한 반은 그 교시에 실제 수업이 있었을 수 있어, 공강으로 채우면 같은 id의 기존 수업을 덮어쓴다.
    private fun fillEmptyPeriods(
        timetables: List<TimetableRedisEntity>,
        skippedRows: List<TimetableInfo>,
    ): List<TimetableRedisEntity> {
        val skippedGroups = skippedRows.map { it.toGroupKeyOrNull() }
        // 어느 반의 행인지조차 알 수 없으면 어떤 반을 덮어쓸지 모르므로 공강을 채우지 않는다.
        if (skippedGroups.any { it == null }) return timetables
        val unfillableGroups = skippedGroups.filterNotNull().toSet()

        return timetables
            .groupBy { Triple(it.date, it.grade, it.classNum) }
            .flatMap { (group, groupedTimetables) ->
                if (group in unfillableGroups) return@flatMap groupedTimetables
                val existingPeriods = groupedTimetables.associateBy { it.period }
                (FIRST_PERIOD..LAST_PERIOD).map { period ->
                    existingPeriods[period] ?: createEmptyPeriod(groupedTimetables.first(), period)
                }
            }
    }

    private fun TimetableInfo.toGroupKeyOrNull(): Triple<LocalDate, Int, Int>? {
        val date =
            try {
                LocalDate.parse(timetableDate, DATE_FORMATTER)
            } catch (_: DateTimeParseException) {
                return null
            }
        val grade = grade.toIntOrNull() ?: return null
        val classNum = classNum.toIntOrNull() ?: return null
        return Triple(date, grade, classNum)
    }

    private fun createEmptyPeriod(
        reference: TimetableRedisEntity,
        period: Int,
    ): TimetableRedisEntity {
        val timetableId =
            "${reference.schoolCode}_${reference.date.format(DATE_FORMATTER)}_${reference.grade}_${reference.classNum}_$period"

        return reference.copy(
            id = timetableId,
            period = period,
            subject = EMPTY_PERIOD_SUBJECT,
        )
    }

    // 형식이 어긋난 행 하나로 전체가 실패하면 재시도해도 결과가 같아, 그 행만 건너뛰고 나머지를 반영한다.
    private fun convertOrSkip(dto: TimetableInfo): TimetableRedisEntity? =
        runCatching { convertToEntity(dto) }
            .onFailure { logger().warn("Skipped invalid timetable row for date {} with reason {}", dto.timetableDate, it.message) }
            .getOrNull()

    private fun convertToEntity(dto: TimetableInfo): TimetableRedisEntity {
        val timetableDate = LocalDate.parse(dto.timetableDate, DATE_FORMATTER)
        val grade = dto.grade.toInt()
        val classNum = dto.classNum.toInt()
        val period = dto.period.toInt()
        val timetableId = "${dto.schoolCode}_${dto.timetableDate}_${grade}_${classNum}_$period"

        return TimetableRedisEntity(
            id = timetableId,
            schoolCode = dto.schoolCode,
            schoolName = dto.schoolName,
            officeCode = dto.officeCode,
            officeName = dto.officeName,
            date = timetableDate,
            academicYear = dto.academicYear,
            semester = dto.semester,
            grade = grade,
            classNum = classNum,
            period = period,
            subject = dto.subject,
        )
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
        private const val FIRST_PERIOD = 1
        private const val LAST_PERIOD = 7
        private const val EMPTY_PERIOD_SUBJECT = "공강"
    }
}
