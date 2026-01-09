package team.themoment.datagsm.resource.domain.neis.schedule.service.impl

import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.neis.schedule.entity.ScheduleRedisEntity
import team.themoment.datagsm.common.domain.neis.schedule.repository.ScheduleRedisRepository
import team.themoment.datagsm.common.dto.neis.internal.SchoolScheduleInfo
import team.themoment.datagsm.common.global.data.NeisEnvironment
import team.themoment.datagsm.resource.domain.neis.schedule.service.SyncScheduleService
import team.themoment.datagsm.resource.global.thirdparty.feign.neis.NeisApiClient
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
                    key = neisEnvironment.key!!,
                    pIndex = pageIndex,
                    pSize = pageSize,
                    atptOfcdcScCode = neisEnvironment.officeCode!!,
                    sdSchulCode = neisEnvironment.schoolCode!!,
                    aa_ymd = null,
                    aa_from_ymd = aaFromYmd,
                    aa_to_ymd = aaToYmd,
                )

            val schedules =
                apiResponse.schoolSchedule
                    ?.find { it.row != null }
                    ?.row
                    ?.map { convertToEntity(it) }
                    ?: emptyList()

            allScheduleEntities.addAll(schedules)
            pageIndex++
        } while (schedules.size == pageSize)

        if (allScheduleEntities.isNotEmpty()) {
            scheduleRedisRepository.deleteAll()
            scheduleRedisRepository.saveAll(allScheduleEntities)
        }
    }

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
    ): List<Int> {
        val grades = mutableListOf<Int>()

        if (grade1Yn == "Y") grades.add(1)
        if (grade2Yn == "Y") grades.add(2)
        if (grade3Yn == "Y") grades.add(3)
        if (grade4Yn == "Y") grades.add(4)
        if (grade5Yn == "Y") grades.add(5)
        if (grade6Yn == "Y") grades.add(6)

        return grades.toList()
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
    }
}
