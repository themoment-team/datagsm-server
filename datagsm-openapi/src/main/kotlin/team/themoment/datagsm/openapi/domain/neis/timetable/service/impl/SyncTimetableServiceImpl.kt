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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

            val timetables =
                apiResponse.hisTimetable
                    ?.find { it.row != null }
                    ?.row
                    ?.map { convertToEntity(it) }
                    ?: emptyList()

            allTimetableEntities.addAll(timetables)
            pageIndex++
        } while (timetables.size == pageSize)

        if (allTimetableEntities.isNotEmpty()) {
            val filledTimetableEntities = fillEmptyPeriods(allTimetableEntities)
            timetableRedisRepository.deleteAll()
            timetableRedisRepository.saveAll(filledTimetableEntities)
        }
    }

    private fun fillEmptyPeriods(timetables: List<TimetableRedisEntity>): List<TimetableRedisEntity> =
        timetables
            .groupBy { Triple(it.date, it.grade, it.classNum) }
            .flatMap { (_, groupedTimetables) ->
                val existingPeriods = groupedTimetables.associateBy { it.period }
                (FIRST_PERIOD..LAST_PERIOD).map { period ->
                    existingPeriods[period] ?: createEmptyPeriod(groupedTimetables.first(), period)
                }
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
