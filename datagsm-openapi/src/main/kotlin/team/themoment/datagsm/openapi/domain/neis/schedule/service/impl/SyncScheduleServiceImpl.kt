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
